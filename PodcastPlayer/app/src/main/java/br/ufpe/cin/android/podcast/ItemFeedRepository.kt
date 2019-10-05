package br.ufpe.cin.android.podcast

import androidx.lifecycle.LiveData

class ItemFeedRepository (private val itemFeedDao: ItemFeedDao) {
    val itemFeeds: LiveData<List<ItemFeed>> = itemFeedDao.getAllItems()

    suspend fun insert(itemFeed: ItemFeed) = itemFeedDao.insert(itemFeed)

    suspend fun update(itemFeed: ItemFeed) = itemFeedDao.update(itemFeed)

    suspend fun unsetPlaying() = itemFeedDao.unsetPlaying()
    suspend fun unsetDownloading() = itemFeedDao.unsetDownloading()

}