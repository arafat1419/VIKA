# VIKA SDK Consumer ProGuard Rules
# These rules are automatically applied to apps that use VIKA SDK

# ============================================
# Keep SDK Public API
# ============================================

# Keep main SDK class and public methods
-keep class com.vika.sdk.VikaSDK { *; }
-keep class com.vika.sdk.VikaSDK$* { *; }

# Keep SDK callbacks and interfaces
-keep interface com.vika.sdk.callbacks.** { *; }
-keep class com.vika.sdk.callbacks.** { *; }

# ============================================
# Keep Models
# ============================================

# Keep public model classes
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
-keep class com.vika.sdk.models.VikaUIOptions { *; }
-keep class com.vika.sdk.models.VikaDisplayMode { *; }
-keep class com.vika.sdk.models.VikaThemeConfig { *; }
-keep class com.vika.sdk.models.VikaLanguage { *; }

# Keep network models for serialization
-keep class com.vika.sdk.network.models.** { *; }
-keepclassmembers class com.vika.sdk.network.models.** {
    @com.google.gson.annotations.SerializedName <fields>;
}

# ============================================
# Keep Navigation Handler Interface
# ============================================

-keep interface com.vika.sdk.navigation.NavigationHandler { *; }

# ============================================
# Socket.IO
# ============================================

-keep class io.socket.** { *; }
-keep interface io.socket.** { *; }
-dontwarn io.socket.**

# ============================================
# Gson Serialization
# ============================================

-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# Prevent stripping of @SerializedName
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# ============================================
# Suppress Warnings
# ============================================

-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
