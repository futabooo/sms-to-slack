# Project Structure

## Organization Philosophy

レイヤードアーキテクチャ（data / domain / ui）をベースに、インフラ層（receiver / worker）を分離した構成。各レイヤーは上位レイヤーからのみ参照される。

## Directory Patterns

### Data Layer
**Location**: `app/src/main/java/com/futabooo/smstoslack/data/`
**Purpose**: データアクセス。Room DB、DataStore、HTTP クライアント、Repository 実装
**Example**: `data/local/dao/FilterRuleDao.kt`, `data/remote/SlackWebhookApi.kt`, `data/repository/SettingsRepository.kt`

### Domain Layer
**Location**: `app/src/main/java/com/futabooo/smstoslack/domain/`
**Purpose**: ビジネスロジックとドメインモデル。Android フレームワーク非依存
**Example**: `domain/model/SmsMessage.kt`, `domain/filter/SmsFilterEvaluator.kt`

### UI Layer
**Location**: `app/src/main/java/com/futabooo/smstoslack/ui/`
**Purpose**: Compose 画面、ViewModel、ナビゲーション、共通コンポーネント
**Example**: `ui/screen/settings/SettingsScreen.kt`, `ui/screen/settings/SettingsViewModel.kt`

### Receiver / Worker
**Location**: `app/src/main/java/com/futabooo/smstoslack/receiver/`, `worker/`
**Purpose**: Android インフラ。BroadcastReceiver、WorkManager Worker
**Example**: `receiver/SmsBroadcastReceiver.kt`, `worker/SlackPostWorker.kt`

### DI
**Location**: `app/src/main/java/com/futabooo/smstoslack/di/`
**Purpose**: 依存性注入コンテナ（手動DI）
**Example**: `di/AppContainer.kt`

## Naming Conventions

- **Files/Classes**: PascalCase（`SmsBroadcastReceiver.kt`）
- **Functions**: camelCase（`evaluateFilter()`）
- **Composable**: PascalCase、画面は `*Screen` サフィックス（`SettingsScreen`）
- **ViewModel**: `*ViewModel` サフィックス（`SettingsViewModel`）
- **DAO**: `*Dao` サフィックス（`FilterRuleDao`）
- **Entity**: `*Entity` サフィックス（`FilterRuleEntity`）
- **Repository**: `*Repository` サフィックス（`FilterRepository`）

## Import Organization

```kotlin
// Android / AndroidX
import android.content.*
import androidx.compose.*

// Third-party
import okhttp3.*
import kotlinx.serialization.*

// Project
import com.futabooo.smstoslack.data.*
import com.futabooo.smstoslack.domain.*
import com.futabooo.smstoslack.ui.*
```

## Code Organization Principles

- **依存方向**: UI → Domain → Data。逆方向の依存は禁止。
- **Domain層はAndroid非依存**: `android.*` の import を含まない純粋Kotlinコード。
- **Repository パターン**: Data層の詳細をUI層から隠蔽。ViewModel は Repository のみを参照。
- **StateFlow**: ViewModel の状態公開は `StateFlow` で行い、Compose は `collectAsState()` で監視。

---
_Document patterns, not file trees. New files following patterns shouldn't require updates_
