# Standard Android optimizations
-dontusemixedcaseclassnames
-verbose
-keepattributes SourceFile,LineNumberTable,Signature,EnclosingMethod,InnerClasses
-renamesourcefileattribute SourceFile

# 1. KEEP YOUR DATA MODELS (Crucial for Retrofit & Gson)
-keep class com.example.forestsnap.data.remote.** { *; }
-keep class com.example.forestsnap.data.local.** { *; }

# 2. KEEP MAPBOX ALIVE
-keep class com.mapbox.** { *; }
-dontwarn com.mapbox.**
-keepattributes *Annotation*

# 3. KEEP GOOGLE ML KIT (Anti-Spoofing)
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

# 4. OKHTTP & RETROFIT
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers interface * {
    @retrofit2.http.* <methods>;
}

# 5. HILT & DAGGER (Dependency Injection)
-keep class dagger.** { *; }
-keep class hilt_aggregated_deps.** { *; }
-dontwarn dagger.**

# Android Defaults
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