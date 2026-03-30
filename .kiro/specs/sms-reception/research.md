# Research & Design Decisions

## Summary
- **Feature**: `sms-reception`
- **Discovery Scope**: New Feature（グリーンフィールド）
- **Key Findings**:
  - Android API 23以上では `SmsMessage.createFromPdu(byte[], String)` を使用し、format は Intent extras から取得する（"3gpp" または "3gpp2"）
  - マニフェスト登録の BroadcastReceiver は端末再起動後も自動的にSMS受信を監視する。`android:exported="true"` と `android:permission="android.permission.BROADCAST_SMS"` の両方が必要
  - Jetpack Compose でのランタイムパーミッション要求は `rememberLauncherForActivityResult` + `ActivityResultContracts.RequestPermission()` で実現する

## Research Log

### SMS_RECEIVED BroadcastReceiver の登録方式
- **Context**: SMS受信をバックグラウンドで検知する方式の調査
- **Sources Consulted**: [Android Developers — Broadcasts Overview](https://developer.android.com/develop/background-work/background-tasks/broadcasts)、[CopyProgramming — BroadcastReceiver 2026 Guide](https://copyprogramming.com/howto/android-broadcast-receiver-does-not-receive-code-example)
- **Findings**:
  - マニフェスト登録（静的）: アプリ非起動時でも受信可能。SMS_RECEIVED はシステムブロードキャストのため制限対象外
  - コンテキスト登録（動的）: アプリ/Activity のライフサイクルに依存。SMS転送用途には不適切
  - Android 12以上では `android:exported` 属性の明示的宣言が必須
  - `android:permission="android.permission.BROADCAST_SMS"` で偽装ブロードキャストを防止
- **Implications**: マニフェスト登録を採用。`goAsync()` は不要（WorkManager にワークを委譲するため onReceive は即座に完了）

### PDUパースとマルチパートSMS
- **Context**: SMS メッセージの正確な抽出方法
- **Sources Consulted**: [Android Developers — SmsMessage](https://developer.android.com/reference/kotlin/android/telephony/SmsMessage)、[W3Tutorials — Multi-part SMS](https://www.w3tutorials.net/blog/android-receiving-long-sms-multipart/)
- **Findings**:
  - `SmsMessage.createFromPdu(pdu, format)` を使用（API 23+、Min SDK 24 なので旧APIの互換性考慮は不要）
  - format は `intent.extras?.getString("format")` で取得（"3gpp" / "3gpp2"）
  - マルチパートSMS: 同一インテントに複数PDUが含まれる。全PDUをイテレートし body を結合する
  - 送信元番号は全PDUで同一のため、最初のPDUから取得すればよい
  - タイムスタンプは `SmsMessage.timestampMillis` で取得（UNIX epoch ミリ秒）
- **Implications**: SmsParser は PDU配列をイテレートし、sender は最初のPDUから、body は全PDUから結合して SmsMessage ドメインモデルを生成する

### ランタイムパーミッション（Jetpack Compose）
- **Context**: RECEIVE_SMS パーミッションの要求フロー
- **Sources Consulted**: [Android Developers — Request runtime permissions](https://developer.android.com/training/permissions/requesting)、[Composables — Permissions Guide](https://composables.com/blog/permissions)
- **Findings**:
  - `rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission())` で要求ランチャーを取得
  - `shouldShowRequestPermissionRationale()` で理由表示の必要性を判定
  - 永続拒否（Don't ask again）の場合は `Settings.ACTION_APPLICATION_DETAILS_SETTINGS` でアプリ設定画面に誘導
  - パーミッション状態は `ContextCompat.checkSelfPermission()` で確認
- **Implications**: パーミッション管理ロジックはUI層（Compose）で実装。状態管理は ViewModel 経由で行う

## Architecture Pattern Evaluation

| Option | Description | Strengths | Risks / Limitations | Notes |
|--------|-------------|-----------|---------------------|-------|
| マニフェスト登録 + WorkManager委譲 | BroadcastReceiver で受信し即座にWorkManagerにワークを渡す | プロセス死後も動作、バッテリー効率良好、リトライ内蔵 | WorkManager の初期化が必要 | steering の tech.md と一致 |
| マニフェスト登録 + goAsync + 直接HTTP | BroadcastReceiver 内で goAsync() 呼び出し後に直接Slack投稿 | シンプル、依存少 | 10秒タイムアウト制限、リトライなし、プロセス死でロスト | 信頼性に問題 |

## Design Decisions

### Decision: マニフェスト登録 BroadcastReceiver
- **Context**: SMS受信をアプリ非起動時でも検知する必要がある
- **Alternatives Considered**:
  1. マニフェスト登録（静的）— アプリ非起動時も動作
  2. コンテキスト登録（動的）— アプリ起動中のみ動作
- **Selected Approach**: マニフェスト登録
- **Rationale**: SMS転送アプリとしてバックグラウンドでの常時動作が必須。SMS_RECEIVED はバックグラウンド制限の対象外
- **Trade-offs**: アンインストールまで常に登録される vs 必要なときだけ登録
- **Follow-up**: なし

### Decision: SmsMessage ドメインモデルの Android 非依存性
- **Context**: domain 層は Android フレームワーク非依存とする（steering structure.md）
- **Selected Approach**: `SmsMessage(sender: String, body: String, timestamp: Long)` を純粋 Kotlin data class として定義
- **Rationale**: テスタビリティと層の独立性を確保
- **Trade-offs**: Android の `android.telephony.SmsMessage` からの変換処理が必要

## Risks & Mitigations
- **Risk**: PDUパースの失敗（不正なPDUデータ） — try-catch で個別PDUのエラーを補足し、成功したパートのみで続行
- **Risk**: パーミッション永続拒否 — アプリ設定画面への導線を UI で提示
- **Risk**: 一部メーカーのカスタムROMでSMSブロードキャストが制限される — ユーザーへバッテリー最適化の除外設定を案内（実装スコープ外だが FAQ に記載）

## References
- [Android Developers — Broadcasts Overview](https://developer.android.com/develop/background-work/background-tasks/broadcasts)
- [Android Developers — SmsMessage API Reference](https://developer.android.com/reference/kotlin/android/telephony/SmsMessage)
- [Android Developers — Request runtime permissions](https://developer.android.com/training/permissions/requesting)
- [Composables — Permissions Guide for Jetpack Compose](https://composables.com/blog/permissions)
