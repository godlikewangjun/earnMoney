package com.wj.makebai.data.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.wj.ktutils.db.*

/**
 * 数据库操作类
 * @author Administrator
 * @version 1.0
 * @date 2019/9/2
 */
class MySqlHelper(ctx: Context) : ManagedSQLiteOpenHelper(ctx, "cache") {
    companion object {
        const val SEARCH_TABLE="search"
        const val READ_TABLE="read"//阅读记录
        const val LIKE_TABLE="like"//点赞过的文章
        var PDCHANNEL = "pd_channel"//文章频道
        private var instance: MySqlHelper? = null

        @Synchronized
        fun getInstance(ctx: Context): MySqlHelper {
            if (instance == null) {
                instance = MySqlHelper(ctx.applicationContext)
            }
            return instance!!
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.createTable(SEARCH_TABLE, true,
            "_id" to INTEGER + PRIMARY_KEY+ AUTOINCREMENT,

            "name" to TEXT,"type" to INTEGER+ DEFAULT("0"))
        db.createTable(READ_TABLE, true,
            "_id" to INTEGER + PRIMARY_KEY,
            "type" to INTEGER,
            "title" to TEXT,
            "value" to TEXT)
        db.createTable(LIKE_TABLE, true,
            "_id" to INTEGER + PRIMARY_KEY,
            "type" to INTEGER,
            "title" to TEXT,
            "value" to TEXT)

        db.createTable(PDCHANNEL, true,
            "articlekey_id" to INTEGER + PRIMARY_KEY,
            "article_key" to TEXT,
            "sort" to INTEGER,
            "type" to INTEGER,
            "describe" to TEXT)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    }

}

// Access property for Context
val Context.database: MySqlHelper
    get() = MySqlHelper.getInstance(applicationContext)