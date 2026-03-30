# Research & Design Decisions

## Summary
- **Feature**: `sms-filtering`
- **Discovery Scope**: Extension（既存 SMS 転送パイプラインへのフィルタ機能追加）
- **Key Findings**:
  - SlackPostWorker.doWork() の転送有効フラグチェック後にフィルタ評価を挿入する統合ポイントを特定
  - SmsFilterEvaluator を Domain 層の純粋関数として実装することで、Android 非依存かつ高テスタビリティを実現
  - AppDatabase のバージョンを 1 → 2 にマイグレーションし FilterRuleEntity を追加。destructive migration で対応（MVP段階のため既存データ消失は許容）

## Research Log

### 既存パイプラインの統合ポイント分析
- **Context**: フィルタ評価をどの位置に挿入すべきか
- **Sources Consulted**: SlackPostWorker.kt、SettingsDataStore.kt、SettingsRepository.kt の既存実装
- **Findings**:
  - SlackPostWorker.doWork() は「転送有効フラグチェック → Webhook URL チェック → フォーマット → Slack 投稿」の順で処理
  - フィルタ評価は転送有効フラグチェックの後、Webhook URL チェックの前に挿入するのが自然
  - ForwardingStatus.FILTERED は既に定義済みで、フィルタ除外時のステータスとして再利用可能
- **Implications**: SlackPostWorker に FilterRepository と SmsFilterEvaluator への依存を追加する

### フィルタモード設定の永続化
- **Context**: フィルタモード（WHITELIST / BLACKLIST / DISABLED）をどこに保存するか
- **Sources Consulted**: 既存 SettingsDataStore の構造
- **Findings**:
  - SettingsDataStore は既に stringPreferencesKey と booleanPreferencesKey を使用
  - フィルタモードは stringPreferencesKey("filter_mode") で enum の name を文字列保存するのが最もシンプル
  - 既存の SettingsDataStore と SettingsRepository を拡張して対応
- **Implications**: SettingsDataStore と SettingsRepository に filterMode 関連のメソッドを追加

### Room データベースマイグレーション
- **Context**: FilterRuleEntity を AppDatabase に追加する際のマイグレーション戦略
- **Sources Consulted**: 既存 AppDatabase（version = 1）
- **Findings**:
  - 現在 version = 1 で ForwardedMessageEntity のみ
  - FilterRuleEntity を追加するには version = 2 へのマイグレーションが必要
  - MVP 段階では fallbackToDestructiveMigration で対応可能（転送履歴の消失は許容）
- **Implications**: AppDatabase の version を 2 に上げ、entities に FilterRuleEntity を追加

## Architecture Pattern Evaluation

| Option | Description | Strengths | Risks / Limitations | Notes |
|--------|-------------|-----------|---------------------|-------|
| 純粋関数 + Repository | SmsFilterEvaluator を Domain 層の純粋関数、FilterRepository を Data 層で実装 | テスタビリティが高い、steering と一致 | 特になし | 採用 |

## Design Decisions

### Decision: フィルタ評価のOR条件
- **Context**: SENDER ルールと KEYWORD ルールの両方が存在する場合の評価方針
- **Alternatives Considered**:
  1. OR 条件 — いずれかにマッチすれば適用
  2. AND 条件 — 両方にマッチしないと適用しない
- **Selected Approach**: OR 条件
- **Rationale**: ユーザーの直感に合致。「この番号またはこのキーワードを含むSMS」をフィルタしたいケースが一般的
- **Trade-offs**: 複雑な条件設定には不向きだが、MVP では十分

### Decision: Destructive Migration
- **Context**: AppDatabase v1 → v2 のマイグレーション方法
- **Selected Approach**: fallbackToDestructiveMigration
- **Rationale**: MVP 段階でユーザーデータの蓄積が少なく、マイグレーションコードの複雑性を回避
- **Trade-offs**: 既存転送履歴が消失する。正式リリース後はプロパーマイグレーションが必要

## Risks & Mitigations
- **Risk**: フィルタルールが大量になった場合のパフォーマンス — 1000件程度なら in-memory 評価で問題なし
- **Risk**: Destructive migration で転送履歴消失 — MVP 段階では許容。リリース後は Migration オブジェクトで対応
- **Risk**: 部分一致の誤マッチ（例: "123" が "01234" にもマッチ）— ユーザーに部分一致であることを UI で明示

## References
- [Android Developers — Room Migration](https://developer.android.com/training/data-storage/room/migrating-db-versions)
- [Android Developers — DataStore](https://developer.android.com/topic/libraries/architecture/datastore)
