package bc.okimatra.soundingcalculator.datasetup

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        PerusahaanEntity::class,
        PenggunaJasaEntity::class,
        PegawaiEntity::class,
        SoundingEntity::class,
        ReportEntity::class,
        KantorEntity::class
    ],
    version = 1
)
@TypeConverters(ArrayListConverter::class)
abstract class UserDatabase:RoomDatabase() {
    abstract fun userDao(): UserDao
        companion object {
            @Volatile
            private var INSTANCE: UserDatabase? = null
            fun getInstance(context: Context): UserDatabase {
                synchronized(this) {
                    var instance = INSTANCE
                    if (instance == null) {
                        instance = Room.databaseBuilder(
                            context.applicationContext,
                            UserDatabase::class.java,
                            "employee_database"
                        ).fallbackToDestructiveMigration().build()
                        INSTANCE = instance
                    }
                    return instance
                }
            }
        }
}