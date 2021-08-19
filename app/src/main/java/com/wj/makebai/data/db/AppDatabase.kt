package com.wj.makebai.data.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.wj.commonlib.app.BaseApplication
import com.wj.makebai.data.mode.db.ReadHistoryMode
import com.wj.makebai.data.mode.db.SearchHistoryMode

/**
 *
 * @author admin
 * @version 1.0
 * @date 2021/2/4
 */
@Database(entities = [SearchHistoryMode::class, ReadHistoryMode::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    companion object{
        val db = Room.databaseBuilder(
            BaseApplication.mApplication!!.get()!!,
            AppDatabase::class.java, "junheling_db_zb"
        ).build()
    }

    abstract fun  searchHistoryDao(): SearchHistoryDao
    abstract fun  readHistoryDao(): ReadHistoryDao
}
