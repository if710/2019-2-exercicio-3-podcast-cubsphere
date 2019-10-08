package br.ufpe.cin.android.podcast

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.io.File

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

    //set an ItemFeed to a specified status
    private fun update (itemFeed: ItemFeed, status: Int) = viewModelScope.launch {
        itemFeed.downloadStatus = status
        repo.update(itemFeed)
    }

    //set an ItemFeed to its default status
    fun updateToDefault(itemFeed: ItemFeed) = update(itemFeed, ItemFeed.DEFAULT)

    //set an ItemFeed to a downloading status
    fun updateToDownloading(itemFeed: ItemFeed) = update(itemFeed, ItemFeed.DOWNLOADING)

    //set an ItemFeed to downloaded status, indicating its media file is downloaded and ready to play
    //also set the path to that ItemFeed's media on local storage
    fun updateDownloadFinished(itemFeed: ItemFeed, fileLocation: String) {
        itemFeed.downloadLocation = fileLocation
        update(itemFeed, ItemFeed.READY)
    }

    //set an ItemFeed to a playing status, indicating the media is currently playing
    fun updatePlay(itemFeed: ItemFeed) = update(itemFeed, ItemFeed.PLAYING)

    //set an ItemFeed to a paused status, and record the time of pause
    fun updatePause(itemFeed: ItemFeed, pauseTime: Int) {
        itemFeed.pauseTime = pauseTime
        update(itemFeed, ItemFeed.READY)
    }

    //delete an ItemFeed's local file, and reset its status to match
    fun updatePlaybackComplete(itemFeed: ItemFeed) {
        itemFeed.pauseTime = 0
        File(itemFeed.downloadLocation).delete()
        itemFeed.downloadLocation = ""
        update(itemFeed, ItemFeed.DEFAULT)
    }

    fun unsetPlaying() = viewModelScope.launch { repo.unsetPlaying() }
    fun unsetDownloading() = viewModelScope.launch { repo.unsetDownloading() }
}