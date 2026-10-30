# Keep Kotlin Serialization models
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}
-keep class kotlinx.serialization.json.** { *; }

# Keep OkHttp & WebSocket reflection
-dontwarn okhttp3.**
-dontwarn okio.**
