

-dontusemixedcaseclassnames
-verbose

-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

-keep class com.example.forestsnap.** { *; }

-keepclasseswithmembernames class * {
    public <init>(...);
}

-keepclassmembers class * {
    static <clinit>();
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclasseswithmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

-keepclasseswithmembernames class **.R$* {
    public static <fields>;
}
