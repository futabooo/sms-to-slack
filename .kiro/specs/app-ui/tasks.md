# Implementation Plan

- [x] 1. DI と Navigation の基盤セットアップ
- [x] 1.1 依存ライブラリを追加し AppContainer と SmsToSlackApplication を実装する
  - Navigation Compose と lifecycle-viewmodel-compose の依存を Version Catalog と build.gradle.kts に追加する
  - AppContainer を実装し、AppDatabase、SettingsDataStore、SettingsRepository、FilterRepository、ForwardedMessageRepository、SlackWebhookApi のシングルトンインスタンスを lazy 初期化で提供する
  - SmsToSlackApplication を実装し、onCreate() で AppContainer を初期化する
  - AndroidManifest.xml の application タグに android:name 属性を追加する
  - _Requirements: 7.1, 7.2, 7.3_
  - _Contracts: AppContainer Service, SmsToSlackApplication State_

- [x] 1.2 Navigation ルートと MainScreen を実装し MainActivity を改修する
  - ナビゲーションルートをダッシュボード、設定、フィルタ管理、転送履歴の4つの文字列定数で定義する
  - MainScreen Composable を実装し、Scaffold 内に NavHost とボトムナビゲーションバーを配置する
  - ボトムナビゲーションバーに4画面のアイテム（Home、Settings、FilterList、History アイコン）を表示し、現在のルートをハイライトする
  - NavHost の各ルートにはプレースホルダーの Text Composable を配置する（後続タスクで実画面に差し替え）
  - MainActivity を改修し、プレースホルダーの Greeting を MainScreen に置き換える
  - ダッシュボードを初期表示ルートに設定する
  - 戻るボタンがナビゲーションスタックに従って動作することを確認する
  - _Requirements: 1.1, 1.2, 1.3, 1.4_
  - _Contracts: Screen Route State, MainScreen State_

- [x] 2. ダッシュボード画面の実装
- [x] 2.1 (P) DashboardViewModel を実装する
  - SettingsRepository と ForwardedMessageRepository を受け取り、転送有効/無効、フィルタモード、Webhook URL 設定状態、直近の転送メッセージを DashboardUiState として StateFlow で公開する
  - Repository の複数 Flow を combine して UI 状態を生成する
  - 転送有効/無効のトグル操作メソッドを提供し、SettingsRepository に即座に保存する
  - ViewModelProvider.Factory を実装し AppContainer からリポジトリを取得できるようにする
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 7.3_
  - _Contracts: DashboardViewModel Service, DashboardUiState State_

- [x] 2.2 DashboardScreen を実装し SmsPermissionHandler を統合する
  - SmsPermissionHandler でコンテンツをラップし、パーミッション未許可時に警告バナーを表示する
  - 転送有効/無効のトグルスイッチを Card コンポーネントで表示する
  - 現在のフィルタモード（WHITELIST / BLACKLIST / DISABLED）を表示する
  - Webhook URL の設定状態（設定済み/未設定）を表示する
  - 直近の転送メッセージ数件をプレビュー表示する
  - NavHost のプレースホルダーを DashboardScreen に差し替える
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 6.1, 6.2, 6.3, 6.4_
  - _Contracts: DashboardScreen State_

- [x] 3. 設定画面の実装
- [x] 3.1 (P) SettingsViewModel を実装する
  - SettingsRepository と SlackWebhookApi を受け取り、Webhook URL、バリデーション結果、フィルタモード、テスト送信状態を SettingsUiState として StateFlow で公開する
  - Webhook URL の入力値をローカル状態として保持し、isValidWebhookUrl() でリアルタイムにバリデーションする
  - 有効な URL が入力された時に DataStore に即座に保存し、無効な URL の場合は保存しない
  - フィルタモード変更時に DataStore に即座に保存する
  - テスト送信メソッドを提供し、Dispatchers.IO で SlackWebhookApi.post() を呼び出して成功/失敗の結果を UiState に反映する
  - ViewModelProvider.Factory を実装し AppContainer からリポジトリと API クライアントを取得できるようにする
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7, 3.8, 7.3_
  - _Contracts: SettingsViewModel Service, SettingsUiState State_

- [x] 3.2 SettingsScreen を実装する
  - OutlinedTextField で Webhook URL を入力・編集できるフィールドを表示し、バリデーションエラー時にエラーメッセージを表示する
  - フィルタモード（WHITELIST / BLACKLIST / DISABLED）の選択 UI を RadioButton で表示する
  - テスト送信ボタンを表示し、送信中は CircularProgressIndicator を表示する
  - テスト送信の成功/失敗結果を表示する
  - NavHost のプレースホルダーを SettingsScreen に差し替える
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7, 3.8_
  - _Contracts: SettingsScreen State_

- [x] 4. フィルタ管理画面の実装
- [x] 4.1 (P) FilterViewModel を実装する
  - FilterRepository を受け取り、フィルタルール一覧とダイアログ表示状態を FilterUiState として StateFlow で公開する
  - FilterRepository.allRulesFlow を stateIn で変換してルール一覧を購読する
  - ルール追加メソッドを提供し、ルール種別とパターン文字列を受け取って FilterRepository に保存する
  - パターン文字列が空の場合はルールの追加を拒否する
  - ルール削除メソッドを提供し、ID 指定で FilterRepository から削除する
  - ルール有効/無効トグルメソッドを提供し、ID と有効状態を FilterRepository に保存する
  - 追加ダイアログと削除確認ダイアログの表示/非表示を管理する
  - ViewModelProvider.Factory を実装し AppContainer から FilterRepository を取得できるようにする
  - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7, 4.8, 7.3_
  - _Contracts: FilterViewModel Service, FilterUiState State_

- [x] 4.2 FilterScreen を実装する
  - LazyColumn で登録済みフィルタルールの一覧を表示し、各ルールのルール種別、パターン文字列、有効/無効状態を表示する
  - FloatingActionButton でルール追加ダイアログを表示し、ルール種別とパターン文字列を入力・確定できるようにする
  - パターン文字列が空の場合はダイアログの確定ボタンを無効化しエラーメッセージを表示する
  - 各ルールに Switch で有効/無効トグルを表示し、切り替え時に即座に更新する
  - ルール削除操作時に確認ダイアログを表示し、確定後に削除を実行する
  - フィルタルールが0件の場合は空状態メッセージを表示する
  - NavHost のプレースホルダーを FilterScreen に差し替える
  - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7, 4.8_
  - _Contracts: FilterScreen State_

- [x] 5. 転送履歴画面の実装
- [x] 5.1 (P) HistoryViewModel を実装する
  - ForwardedMessageRepository を受け取り、転送メッセージ一覧を HistoryUiState として StateFlow で公開する
  - ForwardedMessageRepository.getRecentMessages() を stateIn で変換してメッセージ一覧を購読する
  - ViewModelProvider.Factory を実装し AppContainer から ForwardedMessageRepository を取得できるようにする
  - _Requirements: 5.1, 5.2, 5.3, 5.4, 7.3_
  - _Contracts: HistoryViewModel State, HistoryUiState State_

- [x] 5.2 HistoryScreen を実装する
  - LazyColumn で転送済みメッセージの一覧を新しい順に表示する
  - 各メッセージの送信元、本文プレビュー、タイムスタンプ、転送ステータスを表示する
  - 転送ステータスごとに視覚的に区別できる色分けを適用する（SUCCESS=Green、FAILED=Red、FILTERED=Yellow）
  - 転送履歴が0件の場合は空状態メッセージを表示する
  - NavHost のプレースホルダーを HistoryScreen に差し替える
  - _Requirements: 5.1, 5.2, 5.3, 5.4_
  - _Contracts: HistoryScreen State_

- [ ]* 6. ViewModel のユニットテストを作成する
  - DashboardViewModel の転送トグル切替で SettingsRepository.saveForwardingEnabled が呼ばれることを検証する
  - SettingsViewModel の URL バリデーション結果が UiState に反映されることを検証する
  - SettingsViewModel のテスト送信で SlackWebhookApi.post が呼ばれ結果が UiState に反映されることを検証する
  - FilterViewModel のルール追加・削除・トグルが FilterRepository に委譲されることを検証する
  - _Requirements: 2.1, 2.2, 3.1, 3.2, 3.3, 3.4, 3.7, 3.8, 4.1, 4.4, 4.5, 4.6, 4.7_
