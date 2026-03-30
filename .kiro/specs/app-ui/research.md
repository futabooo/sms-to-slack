# Research & Design Decisions

## Summary
- **Feature**: `app-ui`
- **Discovery Scope**: Extension（既存 Data/Domain 層の上に UI 層を構築）
- **Key Findings**:
  - Navigation Compose 2.8.5 が最新安定版。Compose BOM 2024.09.00（Compose 1.7.x）と互換
  - lifecycle-viewmodel-compose 2.6.1 で ViewModel の Compose 統合が可能
  - 既存 SmsPermissionHandler はダッシュボード画面にそのまま統合可能

## Research Log

### Navigation Compose バージョン互換性
- **Context**: Compose BOM 2024.09.00 との互換性確認
- **Sources Consulted**: [Maven Repository](https://mvnrepository.com/artifact/androidx.navigation/navigation-compose), [Android Developers Navigation](https://developer.android.com/jetpack/androidx/releases/navigation)
- **Findings**:
  - Navigation Compose 2.8.5 が最新安定版（Compose 1.7.0+ 必要）
  - BOM 2024.09.00 は Compose 1.7.x を含むため互換性あり
  - 型安全ルート（@Serializable）が 2.8.x で安定化済み、ただし文字列ルートも引き続きサポート
- **Implications**: 文字列ルートで十分（画面4つの規模では型安全ルートは過剰）

### ViewModel の DI パターン
- **Context**: Hilt 不使用での ViewModel へのリポジトリ注入方法
- **Sources Consulted**: Android Developers ViewModel ドキュメント
- **Findings**:
  - ViewModelProvider.Factory で手動 DI が可能
  - Application クラスに AppContainer を保持するパターンが一般的
  - Compose では `viewModel()` 関数に Factory を渡す
- **Implications**: AppContainer → ViewModelFactory → viewModel() の流れで統一

### 既存コンポーネント再利用
- **Context**: 既存の SmsPermissionHandler と SlackWebhookApi の UI 統合方法
- **Findings**:
  - SmsPermissionHandler は Composable として実装済み。ダッシュボードの content ラッパーとして使用可能
  - SlackWebhookApi.post() は同期関数。テスト送信は ViewModel の viewModelScope + Dispatchers.IO で呼び出す
  - SettingsRepository.isValidWebhookUrl() がバリデーションに使用可能

## Design Decisions

### Decision: 文字列ルート vs 型安全ルート
- **Context**: Navigation Compose 2.8.x は @Serializable 型安全ルートを提供
- **Alternatives Considered**:
  1. 型安全ルート — @Serializable data object でルート定義
  2. 文字列ルート — 従来の文字列定数でルート定義
- **Selected Approach**: 文字列ルート
- **Rationale**: 画面4つで引数なしのシンプルなナビゲーション。型安全ルートの追加設定は過剰
- **Trade-offs**: 型安全性は低いが、シンプルさとコード量の少なさを優先

### Decision: ボトムナビゲーションの実装方式
- **Context**: 4画面の切り替え UI
- **Alternatives Considered**:
  1. NavigationBar（Material 3）+ NavHost
  2. TabRow
  3. NavigationDrawer
- **Selected Approach**: NavigationBar + NavHost
- **Rationale**: Material 3 のベストプラクティスに合致。4画面以下ではボトムナビゲーションが推奨
- **Trade-offs**: 画面が5つ以上になると NavigationDrawer への移行が必要

### Decision: テスト送信の実行方法
- **Context**: 設定画面からの Webhook テスト送信
- **Alternatives Considered**:
  1. ViewModel 内で直接 SlackWebhookApi を呼び出す
  2. WorkManager 経由で送信
- **Selected Approach**: ViewModel 内で直接呼び出し
- **Rationale**: テスト送信は即座にフィードバックが必要。WorkManager のバックグラウンド実行では結果表示が遅延する
- **Trade-offs**: Worker と異なるコードパスになるが、テスト目的なので許容

## Risks & Mitigations
- Navigation Compose 2.8.5 と AGP 9.0.1 の互換性問題 — ビルド確認で早期検証
- ViewModel の Factory パターンがボイラープレート多め — AppContainer で一元化し影響を最小限に
- テスト送信のネットワーク処理が UI スレッドをブロック — Dispatchers.IO + viewModelScope で対処
