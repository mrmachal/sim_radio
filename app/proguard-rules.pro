# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class com.simradio.** {
    *** Companion;
}
-keepclasseswithmembers class com.simradio.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.simradio.**$$serializer { *; }

# Media3
-dontwarn androidx.media3.**