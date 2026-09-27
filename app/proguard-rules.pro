# Project specific R8 rules.
#
# Retrofit, OkHttp, kotlinx.serialization, Room ve Hilt kendi consumer kurallarını AAR içinde taşır;
# bu yüzden burada yalnızca uygulamaya özgü kurallar bulunur.

# Crash raporlarında satır numaraları okunabilsin.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
