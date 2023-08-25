# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

############### Mimi SDK specific rules ################
-keep class io.mimi.sdk.core.model.** { *; }
-keep class io.mimi.sdk.core.api.** { *; }
-keep class io.mimi.hte.HTENativeWrapper { *; }
-keep public enum io.mimi.hte.** { *; }
-keep class * extends io.mimi.sdk.ux.flow.view.Section { <init>(*); } # Keeping all classes extending Section class due to reflection issues with Proguard/R8

################ Kotlin specific rules ################
-keep class kotlin.Metadata { *; }

################ Updated Retrofit rules for R8 full mode ################
# See - https://github.com/square/retrofit/issues/3751

# Keep generic signature of Call, Response (R8 full mode strips signatures from non-kept items).
 -keep,allowobfuscation,allowshrinking interface retrofit2.Call
 -keep,allowobfuscation,allowshrinking class retrofit2.Response

 # With R8 full mode generic signatures are stripped for classes that are not
 # kept. Suspend functions are wrapped in continuations where the type argument
 # is used.
 -keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# Silence missing classes errors: would be fixed with OkHttp 4.11.0.
# See https://github.com/square/okhttp/issues/6258
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

