# Requirements Document

## Introduction
本仕様は、SMS転送パイプラインにおけるフィルタリング機能を定義する。ユーザーが定義したフィルタルール（送信元番号パターン・キーワード）に基づき、受信SMSの転送可否を判定する。ホワイトリスト（許可リスト）モードとブラックリスト（拒否リスト）モードを提供し、フィルタルールはRoomデータベースに永続化する。フィルタ評価エンジンはDomain層に純粋関数として実装し、既存のSlackPostWorkerパイプラインに統合する。

## Requirements

### Requirement 1: フィルタモードの設定
**Objective:** As a ユーザー, I want フィルタリングの動作モードを選択したい, so that 転送するSMSの制御方針を自由に切り替えられる

#### Acceptance Criteria
1. The SettingsRepository shall フィルタモード（WHITELIST / BLACKLIST / DISABLED）をDataStoreに永続化する
2. The SettingsRepository shall フィルタモードのデフォルト値をDISABLED（フィルタ無効＝全SMS転送）とする
3. When ユーザーがフィルタモードを変更した, the SettingsRepository shall 新しいモードを即座にDataStoreに書き込む
4. The SettingsRepository shall フィルタモードをFlowとして公開し、変更を購読可能にする

### Requirement 2: フィルタルールの定義と永続化
**Objective:** As a ユーザー, I want 送信元番号やキーワードによるフィルタルールを作成・管理したい, so that 特定のSMSだけを転送対象にできる

#### Acceptance Criteria
1. The FilterRepository shall フィルタルールをRoomデータベースに永続化する
2. The FilterRuleEntity shall ルール種別（SENDER / KEYWORD）、パターン文字列、有効/無効フラグを含む
3. When ユーザーが新しいフィルタルールを追加した, the FilterRepository shall ルールをデータベースに挿入する
4. When ユーザーがフィルタルールを削除した, the FilterRepository shall ルールをデータベースから削除する
5. When ユーザーがフィルタルールの有効/無効を切り替えた, the FilterRepository shall ルールの状態を更新する
6. The FilterRepository shall 全フィルタルールをFlowとして公開し、変更を購読可能にする

### Requirement 3: 送信元番号によるフィルタリング
**Objective:** As a ユーザー, I want 特定の送信元番号からのSMSだけを転送または除外したい, so that 重要な送信元のメッセージを選別できる

#### Acceptance Criteria
1. When フィルタモードがWHITELISTで送信元番号が有効なSENDERルールにマッチした, the SmsFilterEvaluator shall SMSを転送対象と判定する
2. When フィルタモードがWHITELISTで送信元番号がどのSENDERルールにもマッチしない, the SmsFilterEvaluator shall SMSを転送対象外と判定する
3. When フィルタモードがBLACKLISTで送信元番号が有効なSENDERルールにマッチした, the SmsFilterEvaluator shall SMSを転送対象外と判定する
4. When フィルタモードがBLACKLISTで送信元番号がどのSENDERルールにもマッチしない, the SmsFilterEvaluator shall SMSを転送対象と判定する
5. The SmsFilterEvaluator shall SENDERルールのパターンマッチングで部分一致（contains）を使用する

### Requirement 4: キーワードによるフィルタリング
**Objective:** As a ユーザー, I want SMSメッセージ本文のキーワードで転送を制御したい, so that 特定の内容を含むメッセージだけを転送できる

#### Acceptance Criteria
1. When フィルタモードがWHITELISTでメッセージ本文が有効なKEYWORDルールにマッチした, the SmsFilterEvaluator shall SMSを転送対象と判定する
2. When フィルタモードがWHITELISTでメッセージ本文がどのKEYWORDルールにもマッチしない, the SmsFilterEvaluator shall SMSを転送対象外と判定する
3. When フィルタモードがBLACKLISTでメッセージ本文が有効なKEYWORDルールにマッチした, the SmsFilterEvaluator shall SMSを転送対象外と判定する
4. When フィルタモードがBLACKLISTでメッセージ本文がどのKEYWORDルールにもマッチしない, the SmsFilterEvaluator shall SMSを転送対象と判定する
5. The SmsFilterEvaluator shall KEYWORDルールのパターンマッチングで大文字小文字を区別しない部分一致を使用する

### Requirement 5: 複合フィルタ評価ロジック
**Objective:** As a ユーザー, I want 送信元とキーワードの両方のルールを組み合わせてフィルタリングしたい, so that より精密にSMSを選別できる

#### Acceptance Criteria
1. When フィルタモードがDISABLEDの場合, the SmsFilterEvaluator shall 全てのSMSを転送対象と判定する
2. When 有効なフィルタルールが1件も存在しない場合, the SmsFilterEvaluator shall フィルタモードに関わらず全てのSMSを転送対象と判定する
3. When WHITELISTモードでSENDERルールとKEYWORDルールの両方が存在する場合, the SmsFilterEvaluator shall いずれかのルールにマッチすればSMSを転送対象と判定する（OR条件）
4. When BLACKLISTモードでSENDERルールとKEYWORDルールの両方が存在する場合, the SmsFilterEvaluator shall いずれかのルールにマッチすればSMSを転送対象外と判定する（OR条件）
5. The SmsFilterEvaluator shall Android フレームワークに依存しない純粋関数として実装する

### Requirement 6: SMS転送パイプラインとの統合
**Objective:** As a ユーザー, I want フィルタ設定が自動的にSMS転送に反映されてほしい, so that フィルタ変更後の受信SMSから即座に適用される

#### Acceptance Criteria
1. When SlackPostWorkerがSMSを処理する際, the SlackPostWorker shall フィルタ評価を転送有効フラグチェックの後、Slack投稿の前に実行する
2. When フィルタ評価でSMSが転送対象外と判定された, the SlackPostWorker shall 転送履歴にFILTEREDステータスで記録しResult.success()を返す
3. When フィルタ評価でSMSが転送対象と判定された, the SlackPostWorker shall 通常のSlack投稿処理を続行する
4. While フィルタモードがDISABLEDに設定されている, the SlackPostWorker shall フィルタ評価をスキップし全SMSを転送する
