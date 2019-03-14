package org.mozilla.check.n.share.persistence

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ShareDao {

    @Query("SELECT * FROM share WHERE id = :id")
    fun getShare(id: Long): LiveData<ShareEntity>

    @Query("SELECT * FROM share WHERE content_text LIKE :contentText")
    fun getShare(contentText: String): LiveData<ShareEntity>

    @Query("SELECT * from share")
    fun getShares(): LiveData<List<ShareEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun addShare(shareEntity: ShareEntity): Long

    @Update(onConflict = OnConflictStrategy.IGNORE)
    fun updateShare(shareEntity: ShareEntity): Int

}