# Add project specific ProGuard rules here.
# Keep Moshi-generated adapters and data classes used for JSON parsing.
-keep class com.example.manascan.data.** { *; }
-keepclassmembers class com.example.manascan.data.** { *; }
-keep @com.squareup.moshi.JsonClass class * { *; }
-dontwarn okhttp3.**
-dontwarn okio.**
