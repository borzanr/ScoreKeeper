# kotlinx.serialization keeps generated serializers reachable
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class com.scorekeeper.app.rules.** {
    *** Companion;
}
-keepclasseswithmembers class com.scorekeeper.app.rules.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# O Room carrega a implementação gerada do banco via Class.forName("<Database>_Impl"),
# uma referência que o R8 não enxerga. Vale para o nosso AppDatabase e para o
# WorkDatabase que o SDK do AdMob arrasta — sem estas regras o app morre no start
# com "Failed to create an instance of androidx.work.impl.WorkDatabase".
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-keep class androidx.work.impl.WorkDatabase_Impl { <init>(); }
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public static <fields>;
}
