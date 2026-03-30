# Research & Design Decisions

## Summary
- **Feature**: `slack-integration`
- **Discovery Scope**: Extension（既存 sms-reception 基盤への統合）
- **Key Findings**:
  - Slack Incoming Webhook は `{"text": "..."}` の単純な JSON POST で投稿可能。Content-Type: application/json が必須
  - DataStore Preferences は Kotlin Flow で変更を監視可能。CoroutineScope 内での edit() 呼び出しが推奨
  - OkHttp の同期呼び出し（`execute()`）は CoroutineWorker の `doWork()` 内で安全に使用可能（IO ディスパッチャー上で実行）

## Research Log

### Slack Incoming Webhook ペイロード形式
- **Context**: SMS を Slack に投稿するための API 仕様確認
- **Sources Consulted**: [Slack Developer Docs — Sending messages using incoming webhooks](https://docs.slack.dev/messaging/sending-messages-using-incoming-webhooks/)、[Slack API — Message payloads](https://api.slack.com/reference/messaging/payload)
- **Findings**:
  - 最小ペイロード: `{"text": "message"}` で投稿可能
  - mrkdwn 記法: `*bold*`, `>quote`, `` `code` `` が利用可能
  - Block Kit でリッチメッセージも可能だが、MVP では text フィールドで十分
  - HTTP 200 + "ok" レスポンスが成功。エラー時は非200ステータス + エラーメッセージ
  - Rate limit: 1 message per second per webhook（通常のSMS頻度では問題なし）
- **Implications**: SlackWebhookApi は text フィールドのみの単純な JSON POST で実装。将来的に Block Kit 対応は拡張可能

### DataStore Preferences による設定永続化
- **Context**: Webhook URL と転送フラグの保存方式
- **Sources Consulted**: [Android Developers — DataStore](https://developer.android.com/topic/libraries/architecture/datastore)、[Medium — Mastering Jetpack DataStore in 2025](https://medium.com/design-bootcamp/mastering-jetpack-datastore-in-2025-replace-sharedpreferences-with-modern-apis-b065d2addd9e)
- **Findings**:
  - `dataStore.data` は `Flow<Preferences>` を返し、変更を自動通知
  - `dataStore.edit { }` で非同期書き込み（suspend 関数）
  - stringPreferencesKey / booleanPreferencesKey で型安全なキー定義
  - ViewModel から viewModelScope.launch 内で edit() を呼ぶのが推奨パターン
- **Implications**: SettingsDataStore を DataStore Preferences のラッパーとして実装。webhookUrl と forwardingEnabled の2つのキーを管理

### OkHttp + CoroutineWorker の統合パターン
- **Context**: WorkManager Worker 内での HTTP 通信方式
- **Sources Consulted**: [Android Developers — Threading in CoroutineWorker](https://developer.android.com/develop/background-work/background-tasks/persistent/threading/coroutineworker)、[OkHttp Recipes](https://square.github.io/okhttp/recipes/)
- **Findings**:
  - CoroutineWorker.doWork() は Dispatchers.Default で実行される
  - withContext(Dispatchers.IO) でブロッキング I/O を安全に実行可能
  - OkHttp の execute()（同期）を CoroutineWorker 内で使用可能
  - WorkManager のリトライは setBackoffCriteria で指数バックオフを設定可能
- **Implications**: SlackPostWorker 内で withContext(Dispatchers.IO) を使って OkHttp の同期呼び出しを実行

## Architecture Pattern Evaluation

| Option | Description | Strengths | Risks / Limitations | Notes |
|--------|-------------|-----------|---------------------|-------|
| Repository + DataStore + OkHttp | SettingsRepository が DataStore を、SlackWebhookApi が OkHttp をラップ | steering と一致、関心の分離が明確 | Repository 数が増加 | 採用 |

## Design Decisions

### Decision: OkHttp 同期呼び出し（CoroutineWorker内）
- **Context**: Worker 内での HTTP 通信方式の選択
- **Alternatives Considered**:
  1. OkHttp 同期（execute）+ withContext(Dispatchers.IO)
  2. OkHttp 非同期（enqueue + コールバック）
- **Selected Approach**: 同期呼び出し + withContext(Dispatchers.IO)
- **Rationale**: CoroutineWorker は suspend 関数をサポートしており、同期呼び出しの方がコードがシンプル。Result の返却も直線的に書ける
- **Trade-offs**: スレッドブロッキングが発生するが、IO ディスパッチャー上なので問題なし

### Decision: Room による転送履歴の保存
- **Context**: 転送履歴の永続化と一覧表示
- **Selected Approach**: Room + Entity + DAO + Repository
- **Rationale**: 構造化データの保存・クエリ・ページングに Room が最適。steering tech.md で決定済み
- **Trade-offs**: スキーママイグレーション管理が必要だが、初期バージョンでは不要

## Risks & Mitigations
- **Risk**: Slack Webhook URL が無効化される — テスト送信機能でユーザーが事前検証可能
- **Risk**: ネットワーク長期不通時のワーク蓄積 — WorkManager が自動管理。古いワークは一定期間後に失敗扱い
- **Risk**: 転送履歴 DB の肥大化 — 最大1000件の自動削除で制御

## References
- [Slack Developer Docs — Incoming Webhooks](https://docs.slack.dev/messaging/sending-messages-using-incoming-webhooks/)
- [Android Developers — DataStore](https://developer.android.com/topic/libraries/architecture/datastore)
- [Android Developers — CoroutineWorker Threading](https://developer.android.com/develop/background-work/background-tasks/persistent/threading/coroutineworker)
- [OkHttp — Recipes](https://square.github.io/okhttp/recipes/)
