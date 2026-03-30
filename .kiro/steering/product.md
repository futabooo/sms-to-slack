# Product Overview

Android端末で受信したSMSメッセージをSlackチャンネルに自動転送するモバイルアプリケーション。個人開発者やチーム運用者が、2FA認証コード・アラート通知・重要なSMSをSlackで一元管理できるようにする。

## Core Capabilities

- **SMS受信監視**: バックグラウンドでSMSを受信し、リアルタイムに検知する
- **Slack Webhook投稿**: Incoming Webhookを使って受信SMSをSlackチャンネルに転送する
- **SMSフィルタリング**: 送信元番号やキーワードによるホワイトリスト/ブラックリストでフィルタリングする
- **転送履歴管理**: 転送済みメッセージの履歴と成功/失敗ステータスを管理する

## Target Use Cases

- 2FA認証コードを含むSMSをSlackで即座に確認したい開発者
- サーバーアラートSMSをチームのSlackチャンネルで共有したい運用担当者
- 特定の送信元からのSMSのみを選択的に転送したいユーザー

## Value Proposition

クラウドバックエンドなしで動作するシンプルな単機能アプリ。Slack Incoming Webhook URLを設定するだけで即座に利用開始でき、フィルタ機能で必要なSMSだけを転送できる。

---
_Focus on patterns and purpose, not exhaustive feature lists_
