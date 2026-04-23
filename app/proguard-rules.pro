# Add project specific ProGuard rules here.
# Keep Retrofit & Gson models
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.sopanha.weatherapp.data.model.** { *; }
-keep interface retrofit2.** { *; }
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
