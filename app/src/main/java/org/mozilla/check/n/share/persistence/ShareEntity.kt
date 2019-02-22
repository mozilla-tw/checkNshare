package org.mozilla.check.n.share.persistence

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "share",
    indices = [
        Index("content_text", unique = true)
    ])
data class ShareEntity constructor (
    @PrimaryKey(autoGenerate = true) val id: Long,
    @ColumnInfo(name = "content_text") val contentText: String,
    @ColumnInfo(name = "cofacts_response") val cofactsResponse: Int
    ) {
    constructor(contentText: String, cofactsResponse: Int = 0)
            : this(0, contentText, cofactsResponse)
}
