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

    @PrimaryKey(autoGenerate = true) var id: Long,
    @ColumnInfo(name = "content_text") var contentText: String,
    @ColumnInfo(name = "cofacts_response") var cofactsResponse: Int,
    @ColumnInfo(name = "cofacts_explanation") var cofactsExplanation: String
    ) {
    constructor(contentText: String, cofactsResponse: Int = -1, cofactsExplanation: String = "")
            : this(id = 0, contentText = contentText, cofactsResponse = cofactsResponse, cofactsExplanation = cofactsExplanation)


    companion object {
        const val KEY_ID : String = "key_long_share_entity_id"
        const val KEY_HIGHLIGHT : String = "key_string_share_entity_highlight"

        const val RESPONSE_TRUE = 2
        const val RESPONSE_FALSE = 1
        const val RESPONSE_NEUTRAL = 0
    }


}
