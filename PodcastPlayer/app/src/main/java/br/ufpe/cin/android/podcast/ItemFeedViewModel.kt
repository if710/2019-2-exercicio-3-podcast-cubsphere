package br.ufpe.cin.android.podcast

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ItemFeedViewModel(app: Application) : AndroidViewModel(app) {
    private val repo: ItemFeedRepository
    val itemFeeds: LiveData<List<ItemFeed>>

    init {
        val dao = ItemFeedDatabase.getDatabase(app).itemFeedDao()
        repo = ItemFeedRepository(dao)
        itemFeeds = repo.itemFeeds
    }

    fun insertAll(feed: List<ItemFeed>) = viewModelScope.launch {
        feed.forEach{
            repo.insert(it)
        }
    }

    private fun update (itemFeed: ItemFeed, status: Int) = viewModelScope.launch {
        itemFeed.downloadStatus = status
        repo.update(itemFeed)
    }

    fun updateToDefault(itemFeed: ItemFeed) = update(itemFeed, ItemFeed.DEFAULT)

    fun updateToDownloading(itemFeed: ItemFeed) = update(itemFeed, ItemFeed.DOWNLOADING)

    fun updateToFinished(itemFeed: ItemFeed, fileLocation: String) {
        itemFeed.downloadLocation = fileLocation
        update(itemFeed, ItemFeed.FINISHED)
    }
}