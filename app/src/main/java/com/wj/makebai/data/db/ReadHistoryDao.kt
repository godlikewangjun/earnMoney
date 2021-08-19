package com.wj.makebai.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.wj.makebai.data.mode.db.ReadHistoryMode

/**
 *
 * @author admin
 * @version 1.0
 * @date 2021/2/4
 */
@Dao
interface ReadHistoryDao {
    @Query("SELECT * FROM ReadHistoryMode where type=:type order by id desc")
    fun getAll(type:Int): LiveData<List<ReadHistoryMode>>

    @Query("SELECT * FROM ReadHistoryMode where title=:title and type=:type")
    fun get(title:String,type:Int): LiveData<ReadHistoryMode>

    @Query("SELECT * FROM ReadHistoryMode WHERE id IN (:userIds)")
    fun loadAllByIds(userIds: IntArray): List<ReadHistoryMode>

    @Insert
    fun insert(users: ReadHistoryMode)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg users: ReadHistoryMode)

    @Delete
    suspend fun delete(user: ReadHistoryMode)

    @Query("DELETE FROM ReadHistoryMode where type=:type")
    fun removeAll(type:Int)

    @Query("DELETE FROM ReadHistoryMode where title=:title and type=:type")
    suspend fun delete(title: String,type:Int)
}