package br.ufpe.cin.android.podcast

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
        const val DEFAULT = 0
        const val DOWNLOADING = 1
        const val FINISHED = 2
    }

    override fun toString(): String {
        return title
    }
}