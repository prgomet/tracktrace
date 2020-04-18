package com.cometengine.tracktrace.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.cometengine.tracktrace.AppInit
@Database(
    entities = [ItemDescription::class, TrackingItem::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    companion object {

        private const val databaseName = "database"

        @Volatile
        private var instance: AppDatabase? = null

        /*private val databaseCallback = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
            }
        }*/

        private fun buildDatabase(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, databaseName)
                //.addCallback(databaseCallback)
                .fallbackToDestructiveMigration()
                .build()

        fun getInstance(): AppDatabase = instance ?: synchronized(this) {
            instance ?: buildDatabase(AppInit.instance).also { instance = it }
        }
    }

    abstract fun getTrackingDao(): TrackingItemDao

    abstract fun getTrackingInfoDao(): ItemDescriptionDao
}