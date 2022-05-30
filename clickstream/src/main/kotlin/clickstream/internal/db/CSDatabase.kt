package clickstream.internal.db

import android.content.Context
import androidx.annotation.GuardedBy
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import clickstream.internal.eventscheduler.CSEventData
import clickstream.internal.eventscheduler.CSEventDataDao
import clickstream.internal.eventscheduler.CSEventDataTypeConverters

/**
 * The Database to store the events sent from the client.
 *
 * The Events are cached, processed and then cleared.
 */
@Database(entities = [CSEventData::class], version = 8)
@TypeConverters(CSEventDataTypeConverters::class)
internal abstract class CSDatabase : RoomDatabase() {

    /**
     * The EventBatchDao holds the communication with the DB
     */
    abstract fun eventDataDao(): CSEventDataDao

    companion object {

        @Volatile
        @GuardedBy("lock")
        private var sInstance: CSDatabase? = null
        private val lock = Any()

        /**
         * The singleton instance of the DB.
         *
         * It creates if the INSTANCE is null else returns the INSTANCE
         *
         * @return INSTANCE - EventBatchDatabase instance
         */
        fun getInstance(context: Context): CSDatabase {
            return sInstance ?: synchronized(lock) {
                sInstance ?: buildDatabase(context).also { sInstance = it }
            }
        }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                CSDatabase::class.java,
                "cs-database.db"
            ).fallbackToDestructiveMigration()
                .build()
    }
}
