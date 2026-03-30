# Requirements Document

## Introduction
本仕様は、Android端末でSMSメッセージを受信・パースし、後続の処理（Slack転送等）に渡すための基盤機能を定義する。BroadcastReceiverによるSMS_RECEIVEDインテントの監視、PDUからのメッセージ抽出、マルチパートSMSの結合、およびランタイムパーミッション管理フローを対象範囲とする。本機能はSMS-to-Slackアプリの最も基礎的なレイヤーであり、他の機能（Slack投稿、フィルタリング、UI）はすべてこの機能に依存する。

## Requirements

### Requirement 1: SMS受信の検知
**Objective:** As a ユーザー, I want 端末がSMSを受信したときにアプリがバックグラウンドで自動的に検知する, so that 手動操作なしでSMSメッセージを後続処理に渡せる

#### Acceptance Criteria
1. When SMS_RECEIVEDブロードキャストを受信した, the SmsBroadcastReceiver shall インテントからSMSデータを取得し後続処理を開始する
2. While アプリがバックグラウンドにある, the SmsBroadcastReceiver shall SMS受信を検知できる
3. While 端末が再起動された後, the SmsBroadcastReceiver shall マニフェスト登録により自動的にSMS受信を監視する
4. The SmsBroadcastReceiver shall AndroidManifestにstaticに登録され、アプリが明示的に起動されていなくても動作する

### Requirement 2: SMSメッセージのパース
**Objective:** As a ユーザー, I want 受信したSMSから送信元番号・本文・受信日時を正確に取得したい, so that 完全なSMS情報をSlackに転送できる

#### Acceptance Criteria
1. When SMSインテントを受信した, the SmsParser shall PDUバイト配列から送信元電話番号、メッセージ本文、タイムスタンプを抽出する
2. When 複数のPDUを含むマルチパートSMSを受信した, the SmsParser shall すべてのパートの本文を正しい順序で結合して単一のメッセージとして返す
3. If PDUのパースに失敗した, the SmsParser shall エラーをログに記録し、パース可能なパートのみで処理を続行する
4. The SmsParser shall 送信元番号・本文・タイムスタンプを含むドメインモデル（SmsMessage）を返す

### Requirement 3: SMSドメインモデル
**Objective:** As a 開発者, I want SMS情報を表す型安全なドメインモデルが欲しい, so that アプリ全体で一貫したデータ構造を使用できる

#### Acceptance Criteria
1. The SmsMessage shall 送信元電話番号（sender: String）、メッセージ本文（body: String）、受信タイムスタンプ（timestamp: Long）を含むデータクラスとする
2. The SmsMessage shall domain層に配置し、Androidフレームワークへの依存を持たない

### Requirement 4: RECEIVE_SMSランタイムパーミッション管理
**Objective:** As a ユーザー, I want アプリが必要なSMSパーミッションを適切に要求してくれる, so that SMS受信機能を正しく有効化できる

#### Acceptance Criteria
1. When アプリが初回起動された, the アプリ shall RECEIVE_SMSパーミッションの状態を確認する
2. If RECEIVE_SMSパーミッションが未付与, the アプリ shall パーミッションの必要性を説明する理由を表示してからシステムダイアログを表示する
3. When ユーザーがパーミッションを許可した, the アプリ shall SMS受信が有効であることをUIに反映する
4. When ユーザーがパーミッションを拒否した, the アプリ shall SMS転送が無効であることを明示し、設定画面へのガイダンスを表示する
5. If ユーザーがパーミッションを永続的に拒否した（Don't ask again）, the アプリ shall 端末のアプリ設定画面を開くための導線を提示する

### Requirement 5: AndroidManifestの構成
**Objective:** As a 開発者, I want SMS受信に必要なパーミッションとレシーバーがManifestに正しく宣言されている, so that システムがアプリにSMSブロードキャストを配信する

#### Acceptance Criteria
1. The AndroidManifest shall RECEIVE_SMSパーミッションを宣言する
2. The AndroidManifest shall INTERNETパーミッションを宣言する（後続のSlack投稿で必要）
3. The AndroidManifest shall SmsBroadcastReceiverをSMS_RECEIVEDインテントフィルタ付きで静的に登録する
4. The SmsBroadcastReceiver shall android.permission.BROADCAST_SMSパーミッション属性を持ち、システムからのブロードキャストのみを受け付ける
