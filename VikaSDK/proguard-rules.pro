# VIKA SDK ProGuard Rules
# Keep public API classes and methods accessible

# ============================================
# Keep SDK Public API
# ============================================

# Keep main SDK class
-keep class com.vika.sdk.VikaSDK {
    public static void initialize(...);
    public static com.vika.sdk.VikaSDK getInstance();
    public static boolean isInitialized();
    public static void destroy();
    public static *** getCurrentUIOptions();
    public *** registerScreen(...);
    public *** registerScreens(...);
    public *** saveScreensAsync(...);
    public void openVikaSDK(...);
    public *** sendConversation(...);
    public *** sendConversationAsync(...);
    public void executeDeepLinkNavigation(...);
    public *** navigateFromQuery(...);
    public *** getRegisteredScreens();
    public void registerNavigationHandler(...);
    public void setConversationListener(...);
    public boolean isSocketConnected();
    public void disconnectSocket();
    public void reconnectSocket();
    public boolean isBackendReady();
    public *** getLanguage();
    public *** getConfig();
}

# Keep SDK callbacks and interfaces
-keep class com.vika.sdk.VikaSDK$* { *; }
-keep interface com.vika.sdk.callbacks.** { *; }
-keep class com.vika.sdk.callbacks.** { *; }

# ============================================
# Keep Models for Serialization
# ============================================

# Keep all public model classes
-keep class com.vika.sdk.models.SDKConfig { *; }
-keep class com.vika.sdk.models.SDKConfig$* { *; }
-keep class com.vika.sdk.models.ScreenRegistration { *; }
-keep class com.vika.sdk.models.NavigationType { *; }
-keep class com.vika.sdk.models.NavigationType$* { *; }
-keep class com.vika.sdk.models.NavigationResult { *; }
-keep class com.vika.sdk.models.NavigationOptions { *; }
-keep class com.vika.sdk.models.VikaResult { *; }
-keep class com.vika.sdk.models.VikaResult$* { *; }
-keep class com.vika.sdk.models.VikaError { *; }
-keep class com.vika.sdk.models.VikaError$* { *; }

# Keep network models (used by Gson for serialization)
-keep class com.vika.sdk.network.models.** { *; }
-keepclassmembers class com.vika.sdk.network.models.** { *; }

# ============================================
# Keep Navigation Handler Interface
# ============================================

-keep interface com.vika.sdk.navigation.NavigationHandler { *; }
-keep class com.vika.sdk.navigation.DeepLinkNavigationHandler { *; }

# ============================================
# Retrofit and Gson
# ============================================

# Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Gson
-keep class com.google.gson.** { *; }
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# Prevent stripping of Gson @SerializedName
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep fields with @SerializedName for proper JSON serialization
-keepclassmembers class com.vika.sdk.network.models.** {
    @com.google.gson.annotations.SerializedName <fields>;
}

# ============================================
# OkHttp
# ============================================

-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# ============================================
# Socket.IO
# ============================================

-keep class io.socket.** { *; }
-keep interface io.socket.** { *; }
-dontwarn io.socket.**

# Keep Socket.IO engine.io classes
-keep class io.socket.engineio.** { *; }
-keep class io.socket.client.** { *; }
-keep class io.socket.emitter.** { *; }

# Keep org.json classes used by Socket.IO
-keep class org.json.** { *; }

# ============================================
# Kotlin Coroutines
# ============================================

-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# ============================================
# Obfuscate Internal Implementation
# ============================================

# Allow obfuscation of internal classes
-repackageclasses 'com.vika.sdk.internal'
-allowaccessmodification

# Obfuscate these internal packages
-keep,allowobfuscation class com.vika.sdk.network.api.** { *; }
-keep,allowobfuscation class com.vika.sdk.network.interceptors.** { *; }
-keep,allowobfuscation class com.vika.sdk.network.exceptions.** { *; }
-keep,allowobfuscation class com.vika.sdk.network.socket.** { *; }
-keep,allowobfuscation class com.vika.sdk.security.** { *; }
-keep,allowobfuscation class com.vika.sdk.session.** { *; }
-keep,allowobfuscation class com.vika.sdk.analytics.** { *; }
-keep,allowobfuscation class com.vika.sdk.utils.** { *; }

# ============================================
# Debugging
# ============================================

# Keep line numbers for stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ============================================
# Suppress Warnings
# ============================================

-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*
