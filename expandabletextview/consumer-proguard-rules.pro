-verbose

# keep class members of R
-keepclassmembers class **.R$* {public static <fields>;}
-keepclassmembernames class * {
    public protected <methods>;
}
