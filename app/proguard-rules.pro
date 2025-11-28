# Add project specific ProGuard rules here.
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Preserve line numbers for debugging stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep Kotlin metadata for reflection
-keep class kotlin.Metadata { *; }

# Room Database
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Keep Room entities
-keep @androidx.room.Entity class *
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Dao interface *
-keepclassmembers class * {
    @androidx.room.* <methods>;
}

# Firebase
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Keep data classes for Room
-keep class com.example.linkedinapp.data.** { *; }

# Keep ViewModels
-keep class com.example.linkedinapp.viewmodel.** { *; }

# Keep composable functions
-keep @androidx.compose.runtime.Composable class *
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}

# WorkManager
-keep class androidx.work.** { *; }
-dontwarn androidx.work.**

# Coil image loading
-keep class coil.** { *; }
-dontwarn coil.**

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# Keep Parcelable implementations
-keep class * implements android.os.Parcelable {
    public static final ** CREATOR;
}
