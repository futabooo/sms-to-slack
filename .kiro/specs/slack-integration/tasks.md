# Implementation Plan

- [x] 1. ライブラリ依存関係とドメインモデルの追加
- [x] 1.1 OkHttp、DataStore Preferences、Room、KSP、kotlinx-serialization の依存関係を追加する
  - gradle/libs.versions.toml に OkHttp 4.12.0、DataStore Preferences 1.1.x、Room 2.6.x、kotlinx-serialization 1.7.x のバージョンとライブラリを宣言する
  - ルート build.gradle.kts に KSP プラグインと kotlinx-serialization プラグインを追加する
  - app/build.gradle.kts に各ライブラリの implementation と KSP の annotationProcessor 依存関係を追加する
  - ビルドが成功することを確認する
  - _Requirements: 1.1, 3.1, 4.1, 6.1_

- [x] 1.2 SlackPayload と ForwardingStatus ドメインモデルを定義する
  - Slack Webhook 用の JSON ペイロードモデルを @Serializable data class として定義し、text フィールドのみを持たせる
  - 転送結果を表す列挙型を SUCCESS（投稿成功）、FAILED（投稿失敗）、FILTERED（転送OFF/フィルタでスキップ）の3状態で定義する
  - ドメイン層（domain/model パッケージ）に配置し、Android フレームワーク非依存とする
  - _Requirements: 3.1, 6.2_
  - _Contracts: SlackPayload State, ForwardingStatus State_

- [x] 2. 設定管理（DataStore + Repository）の実装
- [x] 2.1 (P) SettingsDataStore を実装し設定値を DataStore Preferences で永続化する
  - Webhook URL（String?）と転送有効フラグ（Boolean）の2つの設定値を DataStore Preferences で管理する
  - 各設定値を Flow として公開し、変更を購読可能にする
  - 転送有効フラグのデフォルト値を true に設定する
  - suspend 関数で設定値を書き込む API を提供する
  - data/local/datastore パッケージに配置する
  - _Requirements: 1.1, 1.3, 2.1, 2.4_
  - _Contracts: SettingsDataStore State_

- [x] 2.2 SettingsRepository を実装し設定へのアクセスを統一する
  - SettingsDataStore をラップし、Flow による変更監視と一括取得の両方を提供する
  - Webhook URL の形式バリデーション（https:// で始まる有効な URL 形式か判定）を実装する
  - 転送有効フラグの読み書きを転送し、SlackPostWorker から利用可能にする
  - data/repository パッケージに配置する
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 2.1, 2.2, 2.3, 2.4, 7.4_
  - _Contracts: SettingsRepository Service_

- [ ]* 2.3 (P) SettingsRepository のユニットテストを作成する
  - Webhook URL の保存と Flow による値の取得を検証する
  - 転送有効フラグのデフォルト値が true であることを検証する
  - isValidWebhookUrl が正しい URL 形式で true、不正な形式で false を返すことを検証する
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 2.4, 7.4_

- [x] 3. Slack メッセージフォーマッターの実装
- [x] 3.1 (P) SlackMessageFormatter を実装し SMS を Slack 用テキストに変換する
  - 送信元番号、受信日時（ローカルタイムゾーン）、メッセージ本文を含むフォーマット済みテキストを生成する
  - Slack mrkdwn 記法を使用し、送信元を太字（*sender*）、タイムスタンプを整形表示、本文を引用ブロック（>）で表現する
  - SlackPayload を返す純粋関数として object に実装する
  - worker パッケージに配置する
  - _Requirements: 3.1, 3.2, 3.3_
  - _Contracts: SlackMessageFormatter Service_

- [ ]* 3.2 (P) SlackMessageFormatter のユニットテストを作成する
  - 送信元番号、受信日時、メッセージ本文がフォーマット済みテキストに含まれることを検証する
  - mrkdwn 記法（太字、引用ブロック）が正しく適用されていることを検証する
  - _Requirements: 3.1, 3.2, 3.3_

- [x] 4. Slack Webhook API クライアントの実装
- [x] 4.1 (P) SlackWebhookApi を実装し Slack Webhook に HTTP POST する
  - OkHttp を使用して Webhook URL に Content-Type: application/json で SlackPayload の JSON を POST する
  - HTTP 200 で成功、HTTP 4xx でクライアントエラー、HTTP 5xx またはネットワークエラーでリトライ可能エラーを返す sealed class を定義する
  - 接続・読み取り・書き込みタイムアウトを各30秒に設定する
  - SlackPayload を kotlinx-serialization で JSON 文字列にシリアライズする
  - data/remote パッケージに配置する
  - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 7.1_
  - _Contracts: SlackWebhookApi Service_

- [ ]* 4.2 (P) SlackWebhookApi のユニットテストを作成する
  - HTTP 200 レスポンスで成功結果が返ることを検証する
  - HTTP 4xx レスポンスでクライアントエラー結果が返ることを検証する
  - HTTP 5xx レスポンスでリトライ可能エラー結果が返ることを検証する
  - _Requirements: 4.1, 4.2, 4.3, 4.4_

- [x] 5. 転送履歴（Room データベース）の実装
- [x] 5.1 (P) ForwardedMessageEntity と ForwardedMessageDao を定義し AppDatabase を作成する
  - 転送履歴の Room エンティティを送信元番号、メッセージ本文、受信タイムスタンプ、転送ステータス、Slack レスポンスコードのフィールドで定義する
  - 転送履歴の挿入、新しい順での取得（Flow）、古い履歴の自動削除（最大1000件保持）を行う DAO を定義する
  - Room データベースクラスを作成しエンティティと DAO を登録する
  - data/local パッケージに配置する
  - _Requirements: 6.1, 6.2, 6.3, 6.4_
  - _Contracts: ForwardedMessageEntity State, ForwardedMessageDao Service_

- [x] 5.2 ForwardedMessageRepository を実装し転送履歴への統一アクセスを提供する
  - DAO をラップし、転送結果の保存（insert + 古い履歴の自動削除）を1メソッドで提供する
  - 新しい順にソートされた転送履歴を Flow として公開する
  - data/repository パッケージに配置する
  - _Requirements: 6.1, 6.3, 6.4_
  - _Contracts: ForwardedMessageRepository Service_

- [ ]* 5.3 (P) ForwardedMessageDao のユニットテストを作成する
  - Room in-memory データベースでの挿入と取得を検証する
  - trimOldMessages が1000件を超えた古いレコードを削除することを検証する
  - _Requirements: 6.1, 6.3, 6.4_

- [x] 6. SlackPostWorker の本実装とパイプライン結合
- [x] 6.1 SlackPostWorker の既存スタブを本実装に置き換える
  - SettingsRepository から転送有効フラグと Webhook URL を取得し、転送判定を行う
  - 転送OFF時は転送履歴に FILTERED を記録して Result.success() を返す
  - Webhook URL 未設定時は Result.failure() を返す
  - SlackMessageFormatter でペイロードを生成し、SlackWebhookApi で Slack に投稿する
  - 投稿成功時は転送履歴に SUCCESS を記録して Result.success()、クライアントエラー時は FAILED を記録して Result.failure()、サーバーエラー/ネットワークエラー時は Result.retry() を返す
  - _Requirements: 2.2, 2.3, 5.3, 5.4, 5.5, 5.6, 6.1_
  - _Contracts: SlackPostWorker Service_

- [x] 6.2 SmsBroadcastReceiver にネットワーク制約と指数バックオフ設定を追加する
  - 既存の OneTimeWorkRequest にネットワーク接続済み（NetworkType.CONNECTED）の制約を追加する
  - 指数バックオフのリトライポリシーを WorkRequest に設定する
  - _Requirements: 5.1, 5.2, 5.3_

- [ ]* 6.3 SlackPostWorker の結合テストを作成する
  - モック依存で転送ON + 投稿成功のパスを検証する
  - 転送OFF時のスキップ動作を検証する
  - リトライ可能エラー時に Result.retry() が返ることを検証する
  - _Requirements: 2.2, 5.3, 5.4, 5.5, 5.6_
