package br.ufpe.cin.android.podcast

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ItemFeed::class], version = 1)
abstract class ItemFeedDatabase : RoomDatabase() {
    abstract fun itemFeedDao(): ItemFeedDao

    companion object {
        @Volatile
        private var INSTANCE: ItemFeedDatabase? = null

        fun getDatabase(context: Context): ItemFeedDatabase {
            if (INSTANCE != null) {
                return INSTANCE!!
            }
            synchronized(this) {
                INSTANCE = Room.databaseBuilder(
                    context.applicationContext,
                    ItemFeedDatabase::class.java,
                    "item_feed_database"
                ).allowMainThreadQueries().build()

                return INSTANCE!!
            }
        }
    }
}