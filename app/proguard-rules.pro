# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

-keepclassmembers class * {
  @android.webkit.JavascriptInterface <methods>;
}

-keep class com.whatsapp.stickerimporter.** { *; }
