package br.ufpe.cin.android.podcast

import androidx.lifecycle.LiveData
import androidx.room.*

const val default = ItemFeed.DEFAULT
const val downloading = ItemFeed.DOWNLOADING
const val ready = ItemFeed.READY
const val playing = ItemFeed.PLAYING

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

    @Update
    suspend fun update(itemFeed: ItemFeed)

    @Query("update item_feed set downloadStatus = $ready where downloadStatus == $playing")
    suspend fun unsetPlaying()

    @Query("update item_feed set downloadStatus = $default where downloadStatus == $downloading")
    suspend fun unsetDownloading()
}