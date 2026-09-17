# ProGuard rules
-keep class com.example.smartmarkdownnotes.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
