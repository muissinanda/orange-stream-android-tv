# Proguard rules for ORANGE STREAM TV
-keepclassmembers class * extends android.webkit.WebChromeClient {
   public void onShowCustomView(...);
   public void onHideCustomView(...);
}
