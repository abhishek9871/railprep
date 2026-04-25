# Supabase-kt, Ktor, and kotlinx.serialization decode DTOs by generated
# serializers. Keep serializer companions and metadata used by the runtime.
-keepattributes Signature, InnerClasses, EnclosingMethod, RuntimeVisibleAnnotations, RuntimeInvisibleAnnotations
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class ** {
    *** Companion;
}
-keepclasseswithmembers class ** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.railprep.data.remote.supabase.** { *; }
-keep,includedescriptorclasses class com.railprep.domain.model.** { *; }

# Room local attempt mirror is read by generated DAO/database code and must keep
# entity fields stable through R8.
-keep class com.railprep.feature.tests.offline.** { *; }
-keep class androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-keep @androidx.room.Database class * { *; }

# Hilt worker entry-points are accessed through EntryPointAccessors from
# AutoSubmitWorker, not HiltWorkerFactory.
-keep @dagger.hilt.EntryPoint class * { *; }
-keep @dagger.hilt.InstallIn class * { *; }
-keep class com.railprep.feature.tests.work.** { *; }
-keep class androidx.work.ListenableWorker { *; }

# WebView/YouTube and PDF surfaces use framework callbacks and JS interfaces.
# Keep the wrapper classes intact for release builds.
-keep class com.railprep.feature.learn.youtube.** { *; }
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
-keep class com.railprep.feature.learn.pdf.** { *; }

# Navigation typed routes are kotlinx-serializable route payloads.
-keep class com.railprep.feature.**.navigation.** { *; }
-keep class com.railprep.navigation.** { *; }
