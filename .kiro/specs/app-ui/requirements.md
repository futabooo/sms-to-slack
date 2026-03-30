# Requirements Document

## Introduction
SMS-to-Slack アプリの全画面 UI を Jetpack Compose + Material 3 で構築する。ダッシュボード、設定、フィルタ管理、転送履歴の4画面と Navigation Compose によるナビゲーション、SMS受信パーミッション要求フロー、手動DIコンテナを実装する。既存の Data Layer（SettingsRepository, FilterRepository, ForwardedMessageRepository）および Domain Layer（SmsFilterEvaluator, ドメインモデル群）を ViewModel 経由で UI に接続する。

## Requirements

### Requirement 1: ナビゲーションとアプリ構造
**Objective:** ユーザーとして、アプリ内の各画面にスムーズに遷移したい。そうすることで、設定・フィルタ・履歴の各機能に迷わずアクセスできる。

#### Acceptance Criteria
1.1 The アプリ shall Navigation Compose を使用してダッシュボード、設定、フィルタ管理、転送履歴の4画面間をナビゲーション可能にする
1.2 When アプリが起動した時, the アプリ shall ダッシュボード画面を初期表示する
1.3 The アプリ shall 各画面に遷移するためのボトムナビゲーションバーを表示する
1.4 When 戻るボタンが押された時, the アプリ shall ナビゲーションスタックに従って前の画面に戻る

### Requirement 2: ダッシュボード画面
**Objective:** ユーザーとして、転送状態の概要と最近の転送結果を一目で確認したい。そうすることで、SMSの転送が正常に動作しているか素早く把握できる。

#### Acceptance Criteria
2.1 The ダッシュボード画面 shall SMS転送の有効/無効状態をトグルスイッチで表示する
2.2 When 転送トグルが切り替えられた時, the ダッシュボード画面 shall 転送の有効/無効設定を即座に保存する
2.3 The ダッシュボード画面 shall 現在設定されているフィルタモード（WHITELIST / BLACKLIST / DISABLED）を表示する
2.4 The ダッシュボード画面 shall Webhook URL の設定状態（設定済み/未設定）を表示する
2.5 The ダッシュボード画面 shall 直近の転送メッセージ数件をプレビュー表示する

### Requirement 3: 設定画面
**Objective:** ユーザーとして、Slack Webhook URL とフィルタモードを設定したい。そうすることで、SMS転送の送信先と動作を自分の用途に合わせてカスタマイズできる。

#### Acceptance Criteria
3.1 The 設定画面 shall Slack Webhook URL を入力・編集できるテキストフィールドを表示する
3.2 When Webhook URL が入力された時, the 設定画面 shall URL のバリデーション結果をリアルタイムで表示する
3.3 When 有効な Webhook URL が入力された時, the 設定画面 shall 入力値を即座に DataStore に保存する
3.4 If 無効な Webhook URL が入力された場合, the 設定画面 shall エラーメッセージを表示し保存しない
3.5 The 設定画面 shall フィルタモード（WHITELIST / BLACKLIST / DISABLED）を選択できる UI を表示する
3.6 When フィルタモードが変更された時, the 設定画面 shall 選択値を即座に DataStore に保存する
3.7 The 設定画面 shall Webhook URL のテスト送信ボタンを表示する
3.8 When テスト送信ボタンが押された時, the 設定画面 shall 設定済み Webhook URL にテストメッセージを送信し、成功/失敗の結果を表示する

### Requirement 4: フィルタ管理画面
**Objective:** ユーザーとして、フィルタルールの追加・削除・有効無効の切り替えを行いたい。そうすることで、転送対象のSMSを柔軟に制御できる。

#### Acceptance Criteria
4.1 The フィルタ管理画面 shall 登録済みフィルタルールの一覧を表示する
4.2 The フィルタ管理画面 shall 各ルールのルール種別（SENDER / KEYWORD）、パターン文字列、有効/無効状態を表示する
4.3 When ルール追加ボタンが押された時, the フィルタ管理画面 shall ルール種別とパターン文字列を入力するダイアログを表示する
4.4 When ダイアログで確定が押された時, the フィルタ管理画面 shall 入力されたルールを Room データベースに保存しリストに反映する
4.5 If パターン文字列が空の場合, the フィルタ管理画面 shall ルールの保存を拒否しエラーメッセージを表示する
4.6 When ルールの有効/無効トグルが切り替えられた時, the フィルタ管理画面 shall ルールの有効状態を即座に更新する
4.7 When ルールの削除操作が行われた時, the フィルタ管理画面 shall 確認後にルールを Room データベースから削除する
4.8 While フィルタルールが0件の状態, the フィルタ管理画面 shall ルールが未登録であることを示す空状態メッセージを表示する

### Requirement 5: 転送履歴画面
**Objective:** ユーザーとして、過去に転送されたSMSの一覧と各メッセージのステータスを確認したい。そうすることで、転送の成功/失敗を追跡し問題を特定できる。

#### Acceptance Criteria
5.1 The 転送履歴画面 shall 転送済みメッセージの一覧を新しい順に表示する
5.2 The 転送履歴画面 shall 各メッセージの送信元、本文プレビュー、タイムスタンプ、転送ステータス（SUCCESS / FAILED / FILTERED）を表示する
5.3 The 転送履歴画面 shall 転送ステータスごとに視覚的に区別できるアイコンまたは色分けを表示する
5.4 While 転送履歴が0件の状態, the 転送履歴画面 shall 履歴が存在しないことを示す空状態メッセージを表示する

### Requirement 6: SMSパーミッション要求
**Objective:** ユーザーとして、アプリ起動時にSMS受信パーミッションの許可を求められたい。そうすることで、アプリがSMSを受信し転送を開始できる。

#### Acceptance Criteria
6.1 When アプリが初回起動された時, the アプリ shall RECEIVE_SMS パーミッションの許可をリクエストする
6.2 When パーミッションが拒否された時, the アプリ shall パーミッションが必要な理由を説明するダイアログを表示する
6.3 When パーミッションが永続的に拒否された時, the アプリ shall 端末の設定画面へ誘導するメッセージを表示する
6.4 While パーミッションが未許可の状態, the ダッシュボード画面 shall パーミッションが必要であることを示す警告バナーを表示する

### Requirement 7: 依存性注入とアプリ初期化
**Objective:** 開発者として、ViewModel とリポジトリの依存関係を一元管理したい。そうすることで、各画面で一貫した依存性注入を実現できる。

#### Acceptance Criteria
7.1 The アプリ shall Application クラスで AppContainer（手動DIコンテナ）を初期化する
7.2 The AppContainer shall SettingsRepository、FilterRepository、ForwardedMessageRepository の各インスタンスをシングルトンとして提供する
7.3 The 各ViewModel shall AppContainer から必要なリポジトリを取得して使用する
