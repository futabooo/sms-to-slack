# SMS to Slack

Android端末で受信したSMSを自動的にSlackへ転送するアプリです。フィルタリングルールの設定、転送履歴の確認、転送のON/OFF切り替えなどが可能です。

## 機能

- **SMS自動転送** - 受信したSMSをSlack Webhookで指定チャンネルへ転送
- **フィルタリング** - ホワイトリスト/ブラックリスト方式で送信元や本文キーワードによるフィルタリング
- **転送履歴** - 転送の成功/失敗/フィルタ済みステータスを記録・閲覧
- **リトライ** - ネットワークエラー時の自動リトライ（指数バックオフ）

## 必要な環境

- Android 7.0 (API 24) 以上
- Slack Incoming Webhook URL

## インストール

### リリースビルドから

```bash
git clone https://github.com/futabooo/sms-to-slack.git
cd sms-to-slack
./gradlew assembleRelease
```

生成されたAPKを端末にインストールしてください:
`app/build/outputs/apk/release/app-release.apk`

### デバッグビルド

```bash
./gradlew installDebug
```

## セットアップ

### 1. Slack Webhook URLを取得

1. Slackワークスペースで [Incoming Webhooks](https://api.slack.com/messaging/webhooks) を設定
2. 転送先チャンネルを選択してWebhook URLを取得
3. URL形式: `https://hooks.slack.com/services/TXXXX/BXXXX/XXXXXXXX`

### 2. アプリの初期設定

1. アプリを起動し、SMS受信の権限を許可
2. **Settings** タブでSlack Webhook URLを入力
3. **テスト送信** ボタンでWebhookの接続を確認
4. Dashboardで転送が有効になっていることを確認

### 3. フィルタリング（任意）

**Filters** タブでフィルタルールを設定できます。

| フィルタモード | 動作 |
|---|---|
| DISABLED | すべてのSMSを転送 |
| WHITELIST | ルールに一致するSMSのみ転送 |
| BLACKLIST | ルールに一致するSMSを除外して転送 |

ルールの種類:
- **SENDER** - 送信元の電話番号・名前でフィルタ
- **KEYWORD** - メッセージ本文のキーワードでフィルタ

## 使い方

セットアップ完了後、SMSを受信すると自動的にSlackへ転送されます。

- **Dashboard** - 転送状態の確認、最近の転送履歴の表示
- **Settings** - Webhook URL設定、テスト送信
- **Filters** - フィルタルールの追加・有効/無効・削除
- **History** - 全転送履歴の確認

## 技術スタック

- Kotlin / Jetpack Compose
- Room (データベース)
- DataStore (設定保存)
- WorkManager (バックグラウンド処理)
- OkHttp (HTTP通信)

## ライセンス

MIT
