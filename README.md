# 📦 LogsPlugin - Kotlin Android Logging Library

A lightweight, production-ready Android logging plugin built in Kotlin. Logs are stored locally via Room and synced to a remote server using **gRPC**. Perfect for enterprise-grade apps with offline-first logging, auto-sync when network is back, and periodic background uploads via WorkManager.

---

## ✨ Features

- ⚡ **Real-time + Offline Logging**
- 🔄 **Auto Sync** when network becomes available
- 🕓 **Periodic Upload** every 15 minutes (WorkManager)
- 🪵 **Log Levels**: `DEBUG`, `INFO`, `WARN`, `ERROR`
- 📱 **Custom Device Metadata**
- 🔐 **Secure & Resilient gRPC Uploads**
- 💾 **Room DB** for offline persistence
- 📡 **Network Listener** to trigger log syncing

---

## 🚀 Getting Started

### 1. Add Dependency

Until then, clone this repo and include it in your project manually.

---

### 2. Initialize the Plugin

Call this inside your `Application` class or wherever app-level setup is done:

```kotlin
LogsPlugin.init(
    context = applicationContext,
    config = Config(
        grpcUri = "your.grpc.server.com",
        grpcPort = 443
    ),
    deviceInfo = DeviceInfo(
        model = Build.MODEL,
        osVersion = Build.VERSION.RELEASE,
        appVersion = "1.0.0"
    )
)
```


### 3. Log Events
```kotlin
LogsPlugin.log(
    context = context,
    message = "User clicked the login button",
    level = LogLevel.INFO,
    tag = "LoginActivity"
)
```

### 4. Shutdown the Plugin

Use this when your app logs out or you want to stop background syncing:
```kotlin
LogsPlugin.shutdown(context)
```

## ⚙️ Configuration

| Field        | Type   | Description           |
|--------------|--------|-----------------------|
| `grpcUri`    | String | gRPC server URL       |
| `grpcPort`   | Int    | gRPC server port      |
| `deviceInfo` | Object | Device & app metadata |


## 📁 Project Structure
```
├── LogsPlugin.kt               # Core entry point
├── worker/
│   └── LogUploadWorker.kt     # Periodic background upload
├── grpc/
│   └── GrpcClient.kt          # gRPC logic
├── database/
│   ├── LogDao.kt              # Room DAO
│   └── LogEntity.kt           # Log data model
├── repository/
│   └── LogRepositoryImpl.kt   # Handles sync + storage
├── listeners/
│   └── NetworkListener.kt     # Reacts to connectivity changes
├── utils/
│   ├── ConfigPrefs.kt         # Stores plugin config
│   └── WorkerPrefs.kt         # WorkManager scheduling guard
```

## 🧪 Example Use Case

Logging errors or important events:
```kotlin
LogsPlugin.log(
    context = this,
    message = "API call failed",
    level = LogLevel.ERROR,
    tag = "NetworkManager"
)
```
If offline, logs are saved locally and synced automatically when online again.

---

## 🛠 Built With

- **Kotlin**
- **Room (SQLite)**
- **WorkManager**
- **gRPC**
- **Coroutines**

---

## 🔐 Security & Privacy

- 🔒 gRPC communication is **TLS-encrypted** by default
- 🧩 Minimal metadata is collected (**customizable per use case**)
- 🚫 No personal data is stored unless **explicitly added to custom fields**
