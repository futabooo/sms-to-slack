# Implementation Plan

- [x] 1. ドメインモデルとフィルタ評価エンジンの実装
- [x] 1.1 FilterMode、FilterRuleType、FilterRule ドメインモデルを定義する
  - フィルタモードを WHITELIST / BLACKLIST / DISABLED の3状態で表す列挙型を定義する
  - フィルタルール種別を SENDER / KEYWORD の2状態で表す列挙型を定義する
  - フィルタルールのドメインモデルを id、ルール種別、パターン文字列、有効フラグの4フィールドで定義する
  - domain/model パッケージと domain/filter パッケージに配置し、Android フレームワーク非依存とする
  - _Requirements: 1.1, 1.2, 2.2_
  - _Contracts: FilterMode State, FilterRuleType State, FilterRule State_

- [x] 1.2 SmsFilterEvaluator を実装しフィルタルールに基づく転送可否判定を行う
  - フィルタモードが DISABLED の場合、常に転送対象と判定する
  - 有効なフィルタルールが0件の場合、モードに関わらず常に転送対象と判定する
  - WHITELIST モードでは、いずれかの有効ルールにマッチした場合に転送対象と判定する（OR条件）
  - BLACKLIST モードでは、いずれかの有効ルールにマッチした場合に転送対象外と判定する（OR条件）
  - SENDER ルールは送信元番号の部分一致（contains）で評価する
  - KEYWORD ルールはメッセージ本文の大文字小文字無視の部分一致で評価する
  - Android 非依存の純粋関数として domain/filter パッケージに実装する
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 4.1, 4.2, 4.3, 4.4, 4.5, 5.1, 5.2, 5.3, 5.4, 5.5_
  - _Contracts: SmsFilterEvaluator Service_

- [ ]* 1.3 (P) SmsFilterEvaluator のユニットテストを作成する
  - DISABLED モードで常に true が返ることを検証する
  - 有効ルール0件で常に true が返ることを検証する
  - WHITELIST モードで SENDER ルールのマッチ/非マッチを検証する
  - BLACKLIST モードで SENDER ルールのマッチ/非マッチを検証する
  - WHITELIST モードで KEYWORD ルールのマッチ/非マッチを検証する
  - BLACKLIST モードで KEYWORD ルールのマッチ/非マッチを検証する
  - KEYWORD ルールの大文字小文字無視を検証する
  - SENDER + KEYWORD の OR 条件を検証する
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 4.1, 4.2, 4.3, 4.4, 4.5, 5.1, 5.2, 5.3, 5.4_

- [x] 2. 設定管理の拡張（フィルタモード）
- [x] 2.1 (P) SettingsDataStore と SettingsRepository にフィルタモード設定を追加する
  - 既存の SettingsDataStore にフィルタモードの DataStore キー（string）を追加し、FilterMode の名前を文字列で保存する
  - フィルタモードのデフォルト値を DISABLED に設定する
  - フィルタモードを Flow として公開し、変更を購読可能にする
  - suspend 関数でフィルタモードを書き込む API を提供する
  - 既存の SettingsRepository にフィルタモードの Flow 公開、保存、一括取得のメソッドを追加する
  - _Requirements: 1.1, 1.2, 1.3, 1.4_
  - _Contracts: SettingsDataStore State, SettingsRepository Service_

- [x] 3. フィルタルール永続化（Room）の実装
- [x] 3.1 (P) FilterRuleEntity と FilterRuleDao を定義し AppDatabase を v2 にマイグレーションする
  - フィルタルールの Room エンティティをルール種別、パターン文字列、有効フラグのフィールドで定義する
  - 挿入、ID指定削除、有効/無効更新、全件取得（Flow）、有効ルールのみ取得の DAO を定義する
  - 既存の Converters クラスに FilterRuleType の TypeConverter を追加する
  - AppDatabase の entities に FilterRuleEntity を追加し、version を 2 に更新する
  - fallbackToDestructiveMigration を追加し、filterRuleDao の抽象メソッドを追加する
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6_
  - _Contracts: FilterRuleEntity State, FilterRuleDao Service_

- [x] 3.2 FilterRepository を実装しフィルタルールへの統一アクセスを提供する
  - DAO をラップし、ルールの追加・削除・有効無効切替の操作を提供する
  - 全ルールを Flow として公開する
  - 有効なルールのみをドメインモデル（FilterRule）にマッピングして返すメソッドを提供する
  - data/repository パッケージに配置する
  - _Requirements: 2.1, 2.3, 2.4, 2.5, 2.6_
  - _Contracts: FilterRepository Service_

- [ ]* 3.3 (P) FilterRuleDao のユニットテストを作成する
  - Room in-memory データベースでルールの挿入と全件取得を検証する
  - ID 指定削除が正しく動作することを検証する
  - 有効/無効切替が正しく動作することを検証する
  - getEnabledRules が有効ルールのみを返すことを検証する
  - _Requirements: 2.1, 2.3, 2.4, 2.5, 2.6_

- [x] 4. SlackPostWorker へのフィルタ評価統合
- [x] 4.1 SlackPostWorker にフィルタ評価ステップを追加する
  - FilterRepository への依存を追加し、有効なフィルタルールを取得する
  - 転送有効フラグチェックの後、Webhook URL チェックの前にフィルタ評価を実行する
  - フィルタモードが DISABLED の場合はフィルタ評価をスキップする
  - フィルタ評価で転送対象外と判定された場合は転送履歴に FILTERED ステータスで記録し Result.success() を返す
  - フィルタ評価で転送対象と判定された場合は通常の Slack 投稿処理を続行する
  - フィルタルール取得に失敗した場合は安全側に倒し全 SMS を転送する
  - _Requirements: 6.1, 6.2, 6.3, 6.4_
  - _Contracts: SlackPostWorker Service_

- [ ]* 4.2 SlackPostWorker のフィルタ統合テストを作成する
  - フィルタモード DISABLED で全 SMS が転送されることを検証する
  - WHITELIST モードでマッチする SMS が転送されることを検証する
  - BLACKLIST モードでマッチする SMS が除外されることを検証する
  - フィルタ除外時に FILTERED ステータスで履歴記録されることを検証する
  - _Requirements: 6.1, 6.2, 6.3, 6.4_
