# Standard Android ProGuard obfuscation rules
-dontusemixedcaseclassnames
-verbose
-keepattributes SourceFile,LineNumberTable,Signature,EnclosingMethod,InnerClasses
-renamesourcefileattribute SourceFile

# Data Models & Entities (Preserved for Gson serialization and Room ORM)
-keep class com.example.forestsnap.data.remote.** { *; }
-keep class com.example.forestsnap.data.local.** { *; }

# Mapbox Maps SDK
-keep class com.mapbox.** { *; }
-dontwarn com.mapbox.**
-keepattributes *Annotation*

# Google ML Kit Image Labeling
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

# OkHttp & Retrofit Networking
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers interface * {
    @retrofit2.http.* <methods>;
}

# Hilt & Dagger Dependency Injection
-keep class dagger.** { *; }
-keep class hilt_aggregated_deps.** { *; }
-dontwarn dagger.**

# Android Framework Reflection & Native Method Rules
-keepclasseswithmembernames class * { public <init>(...); }
-keepclassmembers class * { static <clinit>(); }
-keepclasseswithmembers class * { public <init>(android.content.Context, android.util.AttributeSet); }
-keepclasseswithmembernames class * { native <methods>; }
-keepclasseswithmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}