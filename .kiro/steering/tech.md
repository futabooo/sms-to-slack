# Technology Stack

## Architecture

MVVM + Repository パターン。UI Layer（Compose + ViewModel）→ Domain Layer（フィルタロジック）→ Data Layer（Repository + DataStore + Room）→ Infrastructure（BroadcastReceiver + WorkManager + OkHttp）の4層構成。

## Core Technologies

- **Language**: Kotlin 2.0.21
- **Framework**: Jetpack Compose with Material 3
- **Build**: Android Gradle Plugin 9.0.1, Gradle Kotlin DSL, Version Catalog
- **Min SDK**: 24 (Android 7.0) / **Target SDK**: 36

## Key Libraries

- **Navigation Compose** — 画面遷移
- **DataStore Preferences** — 設定値（Webhook URL等）の永続化
- **Room** — フィルタルール・転送履歴のローカルDB
- **WorkManager** — 信頼性のあるバックグラウンドSlack投稿（リトライ・ネットワーク制約）
- **OkHttp** — Slack Webhook への HTTP POST
- **kotlinx-serialization** — JSON シリアライゼーション
- **KSP** — Room アノテーション処理

## Development Standards

### Type Safety
Kotlin の null safety を活用。`!!` の使用は禁止。

### Code Quality
Compose のベストプラクティスに従う。状態管理は StateFlow + collectAsState。

### Testing
- ユニットテスト: JUnit 4 + Room in-memory database
- UIテスト: Compose Testing (Espresso)
- フィルタロジックは純粋関数として高テスタビリティを確保

## Development Environment

### Required Tools
- Android Studio (最新安定版)
- JDK 11+
- Android SDK 36

### Common Commands
```bash
# Build: ./gradlew assembleDebug
# Test: ./gradlew test
# Instrumented Test: ./gradlew connectedAndroidTest
# Lint: ./gradlew lint
```

## Key Technical Decisions

- **WorkManager > ForegroundService**: 1回のHTTP POSTに常駐サービスは不要。リトライ・プロセス死後の継続を内蔵。
- **OkHttp > Retrofit**: エンドポイント1つにRetrofitは過剰。
- **手動DI > Hilt**: 画面3つ・リポジトリ5つの規模ではAppContainerで十分。
- **DataStore + Room**: 設定値はDataStore、構造化データはRoomと適材適所。
- **RECEIVE_SMS のみ**: READ_SMS は受信ブロードキャストには不要。

---
_Document standards and patterns, not every dependency_
