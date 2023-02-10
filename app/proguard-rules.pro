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

-keep class com.notix.notixsdk.NotixSDK { *; }
-keep class com.notix.notixsdk.NotixInterstitial { *; }
-keep class com.notix.notixsdk.interstitial.InterstitialButton { *; }
-keep class com.notix.notixsdk.interstitial.ClosingSettings { *; }
-keep class com.notix.notixsdk.interstitial.ClosingSettings { *; }
-keep class com.notix.notixsdk.domain.DomainModels { *; }
-keep class com.notix.notixsdk.domain.DomainModels$RequestVars { *; }
-keepclassmembers class com.notix.notixsdk.domain.DomainModels { *; }
-keep class com.notix.notixsdk.INotificationActivityResolver { *; }
-keep class com.notix.notixsdk.NotificationClickHandlerActivity  { public *; }
-keep class com.notix.notixsdk.NotificationParameters  { *; }
-keep class com.notix.notixsdk.NotificationsService  { *; }
-keep class com.notix.notixsdk.providers.NotixAudienceProvider  { *; }
-keep class com.notix.notixsdk.ContentData  { *; }
-keep class com.notix.notixsdk.NotixContentWrapper  { public *; }
-keep class com.notix.notixsdk.NotixFirebaseMessagingService  { public *; }
-keep class com.notix.notixsdk.providers.StorageProvider  { public *; }
-keep class com.notix.notixsdk.* {  *; }
-keep class com.notix.notixsdk.interstitial.* {  *; }



-keepparameternames
-renamesourcefileattribute SourceFile
-keepattributes Exceptions,InnerClasses,Signature,Deprecated,
                SourceFile,LineNumberTable,*Annotation*,EnclosingMethod

# Keep the classes/members we need for client functionality.
-keep @interface androidx.annotation.Keep
-keep @androidx.annotation.Keep class *
-keepclasseswithmembers class * {
  @androidx.annotation.Keep <fields>;
}
-keepclasseswithmembers class * {
  @androidx.annotation.Keep <methods>;
}

# Keep the classes/members we need for client functionality.
-keep @interface com.google.android.gms.common.annotation.KeepForSdk
-keep @com.google.android.gms.common.annotation.KeepForSdk class *
-keepclasseswithmembers class * {
  @com.google.android.gms.common.annotation.KeepForSdk <fields>;
}
-keepclasseswithmembers class * {
  @com.google.android.gms.common.annotation.KeepForSdk <methods>;
}

# Keep Enum members implicitly
-keepclassmembers @androidx.annotation.Keep public class * extends java.lang.Enum {
    public <fields>;
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keepclassmembers @com.google.android.gms.common.annotation.KeepForSdk class * extends java.lang.Enum {
    public <fields>;
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keepclassmembers @com.google.firebase.annotations.PublicApi class * extends java.lang.Enum {
    public <fields>;
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Implicitly keep methods inside annotations
-keepclassmembers @interface * {
    public <methods>;
}


