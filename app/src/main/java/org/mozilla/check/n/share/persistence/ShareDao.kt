package org.mozilla.check.n.share.persistence

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ShareDao {

    @Query("SELECT * FROM share WHERE id = :rowid")
    fun getShare(rowid: Long): ShareEntity

    @Query("SELECT * from share")
    fun getShares(): LiveData<List<ShareEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addShare(screenshot: ShareEntity)


}