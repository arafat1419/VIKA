# VIKA Navigation SDK

Voice-powered navigation SDK for Android applications. Enable users to navigate to screens using
natural language voice commands.

## Features

- **Voice-Based Navigation**: Record voice commands and navigate to registered screens
- **Deep Link Support**: Navigate using deep links with scheme validation
- **Security**: AES-256 encryption, PBKDF2 key derivation, certificate pinning
- **Analytics**: Track navigation events and errors
- **Jetpack Compose UI**: Modern voice recording interface with waveform visualization

## Requirements

- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 36
- **Kotlin**: 2.0.21+
- **Permissions**: `android.permission.RECORD_AUDIO`

## Installation

Add the dependency to your app's `build.gradle.kts`:

```kotlin
dependencies {
    implementation(project(":VikaSDK"))
    // Or when published:
    // implementation("com.vika:sdk:1.0.0")
}
```

## Quick Start

### 1. Initialize the SDK

Initialize in your `Application` class:

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val config = SDKConfig.Builder("your-api-key")
            .minConfidenceThreshold(0.75f)
            .debugMode(BuildConfig.DEBUG)
            .allowedDeepLinkSchemes("myapp")
            .timeout(15000)
            .build()

        VikaSDK.initialize(
            context = this,
            config = config,
            callback = object : VikaSDK.Companion.InitCallback {
                override fun onSuccess() {
                    // SDK ready - register screens
                    registerScreens()
                }

                override fun onError(error: Throwable) {
                    // Handle initialization error
                    Log.e("MyApp", "SDK init failed", error)
                }
            }
        )
    }

    private fun registerScreens() {
        VikaSDK.getInstance().registerScreens(
            listOf(
                ScreenRegistration(
                    screenId = "home",
                    screenName = "Home",
                    description = "Main home screen",
                    deepLink = "myapp://home",
                    navigationType = NavigationType.DeepLink("myapp://home"),
                    keywords = listOf("home", "main", "start")
                ),
                ScreenRegistration(
                    screenId = "profile",
                    screenName = "Profile",
                    description = "User profile screen",
                    deepLink = "myapp://profile",
                    navigationType = NavigationType.DeepLink("myapp://profile"),
                    keywords = listOf("profile", "account", "settings")
                )
            )
        )
    }
}
```

### 2. Open Voice Recording UI

Launch the SDK's voice recording interface:

```kotlin
// In your Activity or Fragment
Button(onClick = {
    VikaSDK.getInstance().openVikaSDK(context)
}) {
    Text("Talk to VIKA")
}
```

### 3. Handle Deep Links

Configure your app to handle deep links in `AndroidManifest.xml`:

```xml

<activity android:name=".HomeActivity">
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data android:scheme="myapp" android:host="home" />
    </intent-filter>
</activity>
```

## Configuration Options

| Option                   | Default  | Description                                       |
|--------------------------|----------|---------------------------------------------------|
| `apiKey`                 | Required | API key for VIKA backend authentication           |
| `minConfidenceThreshold` | 0.7      | Minimum confidence score (0.0-1.0) for navigation |
| `analyticsEnabled`       | true     | Enable navigation analytics tracking              |
| `debugMode`              | false    | Enable debug logging (disable in production)      |
| `timeout`                | 10000    | Network request timeout in milliseconds           |
| `maxRetries`             | 3        | Maximum retry attempts for failed requests        |
| `cacheEnabled`           | true     | Enable local caching                              |
| `certificatePinning`     | null     | Certificate pinning configuration                 |
| `allowedDeepLinkSchemes` | []       | Whitelist of allowed deep link schemes            |

### Certificate Pinning

For enhanced security, configure certificate pinning:

```kotlin
val config = SDKConfig.Builder("your-api-key")
    .certificatePinning(
        "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA="
    )
    .build()
```

### Deep Link Security

Configure allowed schemes to prevent navigation to unauthorized destinations:

```kotlin
val config = SDKConfig.Builder("your-api-key")
    .allowedDeepLinkSchemes("myapp", "myapp-internal")
    .build()
```

## API Reference

### VikaSDK

Main SDK singleton class.

#### Methods

| Method                                  | Description                                  |
|-----------------------------------------|----------------------------------------------|
| `initialize(context, config, callback)` | Initialize SDK with configuration            |
| `getInstance()`                         | Get SDK instance (throws if not initialized) |
| `isInitialized()`                       | Check if SDK is initialized                  |
| `destroy()`                             | Clean up SDK resources                       |
| `registerScreen(screen, callback)`      | Register a single screen                     |
| `registerScreens(screens, callback)`    | Register multiple screens                    |
| `openVikaSDK(context)`                  | Open voice recording UI                      |
| `sendRecording(file, callback)`         | Send recording to backend                    |
| `executeDeepLinkNavigation(deepLink)`   | Execute deep link navigation                 |
| `getRegisteredScreens()`                | Get list of registered screens               |

### ScreenRegistration

Screen definition for navigation.

```kotlin
data class ScreenRegistration(
    val screenId: String,           // Unique identifier
    val screenName: String,         // Display name
    val description: String,        // Description for AI matching
    val deepLink: String,           // Deep link URI
    val navigationType: NavigationType,  // Navigation type
    val keywords: List<String>      // Keywords for matching
)
```

## Permissions

Add to your `AndroidManifest.xml`:

```xml

<uses-permission android:name="android.permission.RECORD_AUDIO" /><uses-permission
android:name="android.permission.INTERNET" />
```

The SDK handles runtime permission requests for `RECORD_AUDIO` automatically.

## Security

### Encryption

- AES-256-CBC encryption for sensitive data
- PBKDF2 key derivation with 10,000 iterations
- Random IV for each encryption operation

### Network Security

- Certificate pinning support
- Request signing
- Response validation
- HTTPS required

### Deep Link Validation

- Scheme whitelist enforcement
- Registered screen validation
- URI format validation

## Threading

- All public methods are safe to call from any thread
- Callbacks are invoked on the Main thread
- Network operations run on IO dispatcher

## Error Handling

The SDK provides typed errors through `NavigationError`:

```kotlin
sealed class NavigationError {
    data class InvalidQuery(val message: String) : NavigationError()
    data class LowConfidence(val confidence: Float) : NavigationError()
    data class ScreenNotFound(val screenId: String) : NavigationError()
    data class NetworkError(val cause: Throwable) : NavigationError()
    data class SecurityError(val message: String) : NavigationError()
    data class UnknownError(val cause: Throwable) : NavigationError()
}
```

## ProGuard

The SDK includes ProGuard rules. If you encounter issues, add to your `proguard-rules.pro`:

```proguard
-keep class com.vika.sdk.** { *; }
```

## Troubleshooting

### SDK not initialized

Ensure you call `VikaSDK.initialize()` in your `Application.onCreate()` before using other SDK
methods.

### Permission denied

The SDK requires `RECORD_AUDIO` permission. Grant it in device settings or ensure your app requests
it properly.

### Navigation not working

1. Check that screens are registered with correct deep links
2. Verify your app handles the deep link scheme in AndroidManifest
3. Ensure `allowedDeepLinkSchemes` includes your scheme

### Network errors

1. Check internet connectivity
2. Verify API key is correct
3. Check if certificate pinning is configured correctly

## License

Copyright © 2024 VIKA. All rights reserved.

## Support

For issues and feature requests, contact support@vika.com
