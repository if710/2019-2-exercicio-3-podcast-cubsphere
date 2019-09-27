package br.ufpe.cin.android.podcast

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ItemFeedDao {
    @Query("select * from item_feed order by pubDate desc")
    fun getAllItems(): LiveData<List<ItemFeed>>

    //conflicts are expected to occur as a DownloadTask attempts to insert items previously inserted
    //rss feeds are not expected to edit items that have already been posted
    //therefore, we can ignore these conflicts
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(itemFeed: ItemFeed)

    @Query("delete from item_feed")
    suspend fun deleteAll()
}