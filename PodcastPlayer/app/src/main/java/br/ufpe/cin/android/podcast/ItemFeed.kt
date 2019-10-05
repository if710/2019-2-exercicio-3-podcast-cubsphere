package br.ufpe.cin.android.podcast

import android.content.Intent
import androidx.room.Entity
import androidx.room.PrimaryKey


//most of the attributes of ItemFeed would make for a decent primary key
//title was chosen arbitrarily
@Entity(tableName = "item_feed")
data class ItemFeed(
    @PrimaryKey val title: String,
    val link: String,
    val pubDate: String,
    val description: String,
    val downloadLink: String,
    var downloadStatus: Int = DEFAULT,
    var downloadLocation: String = ""
) {
    companion object {
        const val INVALID = -1
        const val DEFAULT = 0
        const val DOWNLOADING = 1
        const val READY = 2
        const val PLAYING = 3
        fun fromIntent(intent: Intent) : ItemFeed {
            return ItemFeed(
                title = intent.getStringExtra("title")!!,
                link = intent.getStringExtra("link")!!,
                pubDate = intent.getStringExtra("pubDate")!!,
                description = intent.getStringExtra("description")!!,
                downloadLink = intent.getStringExtra("downloadLink")!!,
                downloadStatus = intent.getIntExtra("downloadStatus", INVALID),
                downloadLocation = intent.getStringExtra("downloadLocation")!!
            )
        }
    }

    fun placeIntoIntent(intent: Intent) {
        intent.putExtra("title", title)
        intent.putExtra("link", link)
        intent.putExtra("pubDate", pubDate)
        intent.putExtra("description", description)
        intent.putExtra("downloadLink", downloadLink)
        intent.putExtra("downloadStatus", downloadStatus)
        intent.putExtra("downloadLocation", downloadLocation)
    }

    override fun toString(): String {
        return title
    }
}