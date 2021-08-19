package com.wj.makebai.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.wj.makebai.data.mode.db.SearchHistoryMode
import kotlinx.coroutines.flow.Flow

/**
 *
 * @author admin
 * @version 1.0
 * @date 2021/2/4
 */
@Dao
interface SearchHistoryDao {
    @Query("SELECT * FROM SearchHistoryMode where type=:type order by id desc")
    fun getAll(type:Int): LiveData<List<SearchHistoryMode>>

    @Query("SELECT * FROM SearchHistoryMode where id=:id")
    fun get(id:Int): SearchHistoryMode

    @Query("SELECT * FROM SearchHistoryMode WHERE id IN (:userIds)")
    fun loadAllByIds(userIds: IntArray): List<SearchHistoryMode>

    @Insert
    suspend fun insert(users: SearchHistoryMode)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg users: SearchHistoryMode)

    @Delete
    suspend fun delete(user: SearchHistoryMode)

    @Query("DELETE FROM SearchHistoryMode where type=:type")
    fun removeAll(type:Int)

    @Query("DELETE FROM SearchHistoryMode where name=:name and type=:type")
    suspend fun delete(name: String,type:Int)
}