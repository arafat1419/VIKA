# VIKA

Voice-driven navigation SDK for Android applications.

[![License](https://img.shields.io/badge/License-Proprietary-red.svg)](LICENSE)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-26-brightgreen.svg)](https://developer.android.com/about/versions/oreo)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-36-blue.svg)](https://developer.android.com/about/versions)

> **⚠️ IMPORTANT:** VIKA currently **only supports deep link navigation**. Your app must implement
> Android deep links for VIKA to function. See the [Quick Start](#quick-start) section for
> implementation details.

## Overview

VIKA is an Android SDK that enables apps to navigate to specific screens based on user voice/text
conversations with an AI backend. The SDK uses **deep links** to navigate between screens, requiring
your app to properly configure and handle deep link intents.

### Key Features

- 🎤 **Voice Input** - Built-in recording interface with waveform visualization
- 🤖 **AI-Powered** - Natural language processing for navigation queries
- 🔗 **Deep Link Navigation** - Automatic screen navigation via deep links
- 🔒 **Secure** - AES-256 encryption, certificate pinning, rate limiting
- 📊 **Analytics** - Track navigation events and user interactions
- 🎨 **Customizable UI** - Theme support with Jetpack Compose
- 🌐 **Real-time Updates** - Socket.IO integration for live responses

## Project Structure

```
VIKA/
├── VikaSDK/              # Android Library Module
│   ├── src/main/         # SDK source code
│   └── build.gradle.kts  # SDK build configuration
├── sample-app/           # Sample Application
│   ├── src/main/         # Demo app source
│   └── build.gradle.kts  # App build configuration
└── docs/                 # Documentation
```

## Getting Started

### For Developers Using the SDK

If you want to integrate VIKA SDK into your own Android project:

#### Step 1: Configure GitHub Packages

Add to your project's `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()

        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/arafat1419/VIKA")
            credentials {
                username = project.findProperty("github.user") as String?
                password = project.findProperty("github.token") as String?
            }
        }
    }
}
```

Create or update `local.properties` in your project root:

```properties
github.user=your-github-username
github.token=your-github-personal-access-token
```

> **Note:** GitHub Packages requires authentication even for public repositories. Create
> a [GitHub Personal Access Token](https://github.com/settings/tokens/new) with `read:packages` scope.

#### Step 2: Add Dependency

Add to your app's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.vika.sdk:vika-sdk:1.0.0")
}
```

### For Contributors / Building from Source

If you want to build or contribute to this project:

```bash
# Clone the repository
git clone https://github.com/arafat1419/VIKA.git
cd VIKA

# Build the project (no GitHub credentials needed)
./gradlew build

# Run the sample app
./gradlew :sample-app:installDebug
```

The `sample-app` uses the local `:VikaSDK` module, so no external authentication is required.

### Quick Start

> **⚠️ IMPORTANT:** VIKA currently **only works with deep links**. You must implement deep link
> navigation for VIKA to function. See the `sample-app` module for a complete working example.

#### 1. Initialize SDK

```kotlin
val config = SDKConfig.Builder("YOUR_API_KEY")
    .debugMode(BuildConfig.DEBUG)
    .allowedDeepLinkSchemes("yourapp")
    .build()

VikaSDK.initialize(this, config) { /* SDK ready */ }
```

#### 2. Register Screens

```kotlin
val screens = listOf(
    ScreenRegistration(
        screenId = "home",
        screenName = "Home",
        deepLink = "yourapp://home",
        navigationType = NavigationType.DeepLink("yourapp://home"),
        keywords = listOf("home", "main")
    )
)

VikaSDK.getInstance().registerScreens(screens)
```

#### 3. Setup Deep Links

Configure deep links in `AndroidManifest.xml` and handle them in your Activity. See `sample-app/`
for complete implementation.

#### 4. Open VIKA

```kotlin
VikaSDK.getInstance().openVikaSDK(context)
```

## Documentation

- This README - Complete integration guide
- `sample-app/` - Working example implementation demonstrating SDK usage

## Architecture

### SDK Components

- **Core** - SDK initialization and configuration
- **Navigation** - Deep link handling and screen registration
- **Network** - Retrofit API client with encryption
- **Socket** - Real-time communication via Socket.IO
- **Security** - AES encryption, certificate pinning, rate limiting
- **Analytics** - Event tracking and reporting
- **UI** - Compose-based voice recording interface

### Tech Stack

- **Language:** Kotlin 2.0
- **UI:** Jetpack Compose
- **Networking:** Retrofit 3.0, OkHttp 5.3
- **Real-time:** Socket.IO 2.1
- **Security:** AES-256, PBKDF2, Certificate Pinning
- **Async:** Kotlin Coroutines
- **DI:** Manual dependency injection

## Requirements

- **Min SDK:** 26 (Android 8.0)
- **Target SDK:** 36
- **Compile SDK:** 36
- **Kotlin:** 2.0+
- **Java:** 11

## Security

- AES-256-CBC encryption with random IV
- PBKDF2 key derivation (10,000 iterations)
- Certificate pinning support
- Request signing with SHA-256
- Deep link validation and whitelist
- App signature verification
- Rate limiting
- ProGuard rules included

**Security Best Practices:**

- Never commit `local.properties` (already in `.gitignore`)
- Rotate GitHub tokens every 90 days
- Use read-only tokens for client access
- Store API keys securely (BuildConfig or encrypted storage)

## Sample App

The `sample-app` module demonstrates:

- SDK initialization
- Screen registration
- Voice interface integration
- Deep link handling
- Custom UI theming

Run the sample app to see VIKA in action!

## Contributing

We welcome contributions! To contribute:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

See [CLAUDE.md](CLAUDE.md) for detailed project architecture and development guidelines.

## Support

- **Issues:** [GitHub Issues](https://github.com/arafat1419/VIKA/issues)
- **Discussions:** [GitHub Discussions](https://github.com/arafat1419/VIKA/discussions)
- **Documentation:** See this README and `sample-app/` for integration examples

## License

This SDK is proprietary software. Authorized use only.

**© 2025 VIKA Team. All rights reserved.**

For licensing inquiries, contact the VIKA team.

## Changelog

### Version 1.0.0 (Current)

**Features:**

- Initial SDK release
- Voice recording with waveform visualization
- AI-powered navigation query processing
- Deep link navigation system
- Real-time Socket.IO integration
- AES-256 encryption
- Analytics tracking
- Jetpack Compose UI
- GitHub Packages distribution

**Technical:**

- Kotlin 2.0 with modern compiler options
- Retrofit 3.0 for HTTP requests
- OkHttp 5.3 for networking
- Socket.IO 2.1 for real-time communication
- ProGuard rules for code protection
- Source JAR publishing

---

**Made with ❤️ by the VIKA Team**
