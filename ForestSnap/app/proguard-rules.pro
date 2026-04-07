# This is a configuration file for ProGuard.
# http://proguard.sourceforge.net/index.html#manual/usage.html

-dontusemixedcaseclassnames
-verbose

# Preserve line numbers for debugging stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep custom application classes
-keep class com.example.forestsnap.** { *; }

# Keep all public classes
-keepclasseswithmembernames class * {
    public <init>(...);
}

# Preserve the special static initializer
-keepclassmembers class * {
    static <clinit>();
}

# Preserve custom view constructors used by the Android framework
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

# Preserve all native method names and the names of their classes
-keepclasseswithmembernames class * {
    native <methods>;
}

# Preserve enum constructors
-keepclasseswithmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Preserve custom Parcelable implementations
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Preserve static fields of inner classes of R classes that might be accessed
-keepclasseswithmembernames class **.R$* {
    public static <fields>;
}
