# Requirements Document

## Introduction
本仕様は、sms-reception spec で受信・パースされたSMSメッセージを Slack Incoming Webhook に投稿するための連携機能を定義する。Webhook URLの設定・永続化、SMSからSlackメッセージへのフォーマット変換、WorkManagerによる信頼性のあるバックグラウンド投稿（リトライ・ネットワーク制約）、転送ON/OFF制御、および転送履歴の記録を対象範囲とする。本機能はSMS受信パイプラインの中核であり、sms-reception（入力）とsms-filtering（フィルタ）の間に位置する。

## Requirements

### Requirement 1: Slack Webhook URLの設定と永続化
**Objective:** As a ユーザー, I want Slack Incoming Webhook URLをアプリ内で設定・保存したい, so that SMSの転送先チャンネルを指定できる

#### Acceptance Criteria
1. The SettingsRepository shall Webhook URLをDataStore Preferencesに永続化する
2. When ユーザーがWebhook URLを入力して保存した, the SettingsRepository shall 値を即座にDataStoreに書き込む
3. When アプリが再起動された, the SettingsRepository shall 保存済みのWebhook URLをDataStoreから復元する
4. The SettingsRepository shall Webhook URLをFlowとして公開し、変更を購読可能にする

### Requirement 2: SMS転送のON/OFF制御
**Objective:** As a ユーザー, I want SMS転送を一時的に無効化したい, so that 必要に応じて転送を停止・再開できる

#### Acceptance Criteria
1. The SettingsRepository shall 転送有効フラグ（Boolean）をDataStoreに永続化する
2. When ユーザーが転送を無効にした, the SlackPostWorker shall SMSを受信してもSlackへの投稿を行わない
3. When ユーザーが転送を有効にした, the SlackPostWorker shall 以降の受信SMSをSlackに投稿する
4. The SettingsRepository shall 転送有効フラグのデフォルト値をtrue（有効）とする

### Requirement 3: SMSからSlackメッセージへのフォーマット変換
**Objective:** As a ユーザー, I want 受信SMSがSlackで読みやすい形式で表示されてほしい, so that 送信元・本文・時刻を一目で確認できる

#### Acceptance Criteria
1. The SlackMessageFormatter shall SmsMessage（sender, body, timestamp）をSlack Webhook用のJSONペイロードに変換する
2. The SlackMessageFormatter shall 送信元番号、受信日時（ローカルタイムゾーン）、メッセージ本文を含むフォーマットされたテキストを生成する
3. The SlackMessageFormatter shall Slack mrkdwn記法を使用して送信元とタイムスタンプを強調表示し、本文を引用ブロックで表示する

### Requirement 4: Slack Webhook への HTTP POST
**Objective:** As a 開発者, I want OkHttpを使ってSlack Webhook URLにJSONをPOSTしたい, so that 受信SMSをSlackチャンネルに投稿できる

#### Acceptance Criteria
1. When SlackペイロードとWebhook URLが提供された, the SlackWebhookApi shall Content-Type: application/json でHTTP POSTリクエストを送信する
2. When Slack APIがHTTP 200を返した, the SlackWebhookApi shall 成功結果を返す
3. If Slack APIがHTTP 4xxエラーを返した, the SlackWebhookApi shall エラー情報を含む失敗結果を返す
4. If Slack APIがHTTP 5xxエラーまたはネットワークエラーを返した, the SlackWebhookApi shall リトライ可能な失敗結果を返す
5. If 接続がタイムアウトした, the SlackWebhookApi shall 30秒以内にタイムアウト結果を返す

### Requirement 5: WorkManagerによる信頼性のあるバックグラウンド投稿
**Objective:** As a ユーザー, I want SMSがネットワーク不安定時でも確実にSlackに転送されてほしい, so that メッセージが失われない

#### Acceptance Criteria
1. When SmsBroadcastReceiverがSMSを受信した, the SlackPostWorker shall WorkManagerのOneTimeWorkRequestとしてenqueueされる
2. The SlackPostWorker shall ネットワーク接続済みの制約（NetworkType.CONNECTED）を設定する
3. If Slack投稿がリトライ可能な失敗で終了した, the SlackPostWorker shall Result.retry()を返しWorkManagerの指数バックオフでリトライする
4. If Slack投稿が成功した, the SlackPostWorker shall Result.success()を返す
5. If Slack投稿が回復不能な失敗で終了した, the SlackPostWorker shall Result.failure()を返す
6. While 転送がOFFに設定されている, the SlackPostWorker shall Slack投稿をスキップしResult.success()を返す

### Requirement 6: 転送履歴の記録
**Objective:** As a ユーザー, I want 転送済みSMSの履歴と結果を確認したい, so that 正常に転送されたか把握できる

#### Acceptance Criteria
1. When SlackPostWorkerが処理を完了した, the ForwardedMessageRepository shall 転送結果をRoomデータベースに記録する
2. The ForwardedMessageEntity shall 送信元番号、メッセージ本文、受信タイムスタンプ、転送ステータス（SUCCESS / FAILED / FILTERED）、Slackレスポンスコードを含む
3. The ForwardedMessageRepository shall 転送履歴を新しい順にFlowとして公開する
4. The ForwardedMessageRepository shall 古い履歴を自動削除し、最大1000件を保持する

### Requirement 7: Webhook URLの検証とテスト送信
**Objective:** As a ユーザー, I want 設定したWebhook URLが正しく動作するかテストしたい, so that 設定ミスを事前に検出できる

#### Acceptance Criteria
1. When ユーザーがテスト送信を実行した, the SlackWebhookApi shall テストメッセージをWebhook URLにPOSTする
2. When テスト送信が成功した, the アプリ shall 成功メッセージを表示する
3. If テスト送信が失敗した, the アプリ shall エラーの詳細を表示する
4. If Webhook URLが空または不正なURL形式, the アプリ shall テスト送信を実行せずにバリデーションエラーを表示する
