package clickstream.health.internal.database

import android.content.Context
import androidx.annotation.GuardedBy
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * The Database to store the events sent from the client.
 *
 * The Events are cached, processed and then cleared.
 */
@Database(entities = [CSHealthEventEntity::class], version = 10)
internal abstract class CSHealthDatabase : RoomDatabase() {

    /**
     * The HealthDao holds the communication with the DB
     */
    abstract fun healthEventDao(): CSHealthEventDao

    companion object {

        @Volatile
        @GuardedBy("lock")
        private var sInstance: CSHealthDatabase? = null
        private val lock = Any()

        /**
         * The singleton instance of the DB.
         *
         * It creates if the INSTANCE is null else returns the INSTANCE
         *
         * @return INSTANCE - EventBatchDatabase instance
         */
        fun getInstance(context: Context): CSHealthDatabase {
            return sInstance ?: synchronized(lock) {
                sInstance ?: buildDatabase(context).also { sInstance = it }
            }
        }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                CSHealthDatabase::class.java,
                "cs-health-database.db"
            ).fallbackToDestructiveMigration()
                .build()
    }
}
