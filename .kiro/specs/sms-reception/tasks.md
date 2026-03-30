# Implementation Plan

- [x] 1. SMSドメインモデルとパーサーの実装
- [x] 1.1 (P) SmsMessage ドメインモデルと SmsPermissionStatus 列挙型を定義する
  - sender（String）、body（String）、timestamp（Long）を持つイミュータブルな data class を domain/model パッケージに作成する
  - GRANTED / DENIED / PERMANENTLY_DENIED の3状態を持つ SmsPermissionStatus 列挙型を同パッケージに作成する
  - android.* への依存を含まない純粋 Kotlin コードとする
  - _Requirements: 3.1, 3.2_

- [x] 1.2 SmsParser を実装し PDU 解析とマルチパート SMS 結合を行う
  - Intent extras から PDU バイト配列と format 文字列（"3gpp" / "3gpp2"）を取得する
  - 各 PDU を SmsMessage.createFromPdu(pdu, format) で解析し、送信元番号は最初の PDU から取得する
  - マルチパート SMS の場合は全 PDU の本文を結合して単一の body とする
  - 個別 PDU のパース失敗時はログに記録し、成功したパートのみで処理を続行する
  - 全 PDU のパースに失敗した場合は null を返す
  - receiver パッケージに配置する
  - _Requirements: 2.1, 2.2, 2.3, 2.4_
  - _Contracts: SmsParser Service_

- [ ]* 1.3 (P) SmsParser のユニットテストを作成する
  - シングルパート SMS の正常パースを検証する
  - マルチパート SMS の本文結合を検証する
  - 不正な PDU データでのエラーハンドリングを検証する
  - extras が null または pdus キーが存在しない場合の null 返却を検証する
  - _Requirements: 2.1, 2.2, 2.3, 2.4_

- [x] 2. AndroidManifest 構成と SmsBroadcastReceiver の実装
- [x] 2.1 (P) AndroidManifest にパーミッションとレシーバーの宣言を追加する
  - RECEIVE_SMS パーミッションを宣言する
  - INTERNET パーミッションを宣言する
  - SmsBroadcastReceiver を SMS_RECEIVED インテントフィルタ付きで静的に登録する
  - レシーバーに android:exported="true" と android:permission="android.permission.BROADCAST_SMS" を設定し、システムブロードキャストのみ受付とする
  - _Requirements: 5.1, 5.2, 5.3, 5.4_

- [x] 2.2 SmsBroadcastReceiver を実装し SMS 受信を検知して後続処理に委譲する
  - onReceive で intent.action が SMS_RECEIVED_ACTION であることを検証する
  - SmsParser を呼び出して SmsMessage を取得する
  - 取得した SmsMessage のデータ（sender, body, timestamp）を WorkManager の inputData に設定し OneTimeWorkRequest を enqueue する
  - パース失敗（null 返却）時はログに記録し処理をスキップする
  - receiver パッケージに配置し、マニフェスト登録によりアプリ非起動時・端末再起動後もバックグラウンドで動作する
  - _Requirements: 1.1, 1.2, 1.3, 1.4_
  - _Contracts: SmsBroadcastReceiver Event_

- [x] 3. ランタイムパーミッション管理の実装
- [x] 3.1 (P) RECEIVE_SMS パーミッションの状態確認・要求・結果ハンドリングを行う Composable を実装する
  - アプリ起動時に ContextCompat.checkSelfPermission で RECEIVE_SMS パーミッションの付与状態を確認する
  - 未付与の場合、shouldShowRequestPermissionRationale でパーミッション理由表示の必要性を判定する
  - 理由が必要な場合はパーミッションの必要性を説明するダイアログを表示してからシステムパーミッションダイアログを起動する
  - rememberLauncherForActivityResult と ActivityResultContracts.RequestPermission を使用してパーミッション要求を行う
  - 許可された場合は SMS 受信が有効であることを状態に反映する
  - 拒否された場合は SMS 転送が無効であることを明示し、再要求またはガイダンスを表示する
  - 永続的に拒否された場合（shouldShowRequestPermissionRationale が false）は、端末のアプリ設定画面を開くための導線を提示する
  - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5_
  - _Contracts: PermissionManager State_

- [ ] 4. SMS 受信パイプラインの結合検証
- [ ] 4.1 SmsBroadcastReceiver から WorkManager enqueue までの結合テストを作成する
  - モック Intent を SmsBroadcastReceiver.onReceive に渡し、SmsParser 経由で SmsMessage が生成されることを検証する
  - WorkManager に正しいデータ（sender, body, timestamp）で OneTimeWorkRequest が enqueue されることを検証する
  - SMS_RECEIVED 以外のアクションが無視されることを検証する
  - _Requirements: 1.1, 2.4_
