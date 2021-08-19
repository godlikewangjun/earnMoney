package com.wj.makebai.data.db

import com.wj.commonlib.app.BaseApplication
import com.wj.commonlib.data.mode.ArticleKeyMode
import com.wj.ktutils.db.classParser
import com.wj.ktutils.db.delete
import com.wj.ktutils.db.insert
import com.wj.ktutils.db.select

/**
 * 消息列表数据库操作
 * @author Administrator
 * @version 1.0
 * @date 2018/10/7/007
 */
object PdtDb {
    /**
     * 插入数据
     * @type 0 是已选 1是没选
     */
    fun insert(modes: ArrayList<ArticleKeyMode>, type: Int?) {
        if(modes.isEmpty())return
        MySqlHelper.getInstance(BaseApplication.mApplication!!.get()!!).use {
            delete(MySqlHelper.PDCHANNEL, "type=$type")
            for (index in modes) {
                insert(MySqlHelper.PDCHANNEL, "sort" to index.sort
                        , "article_key" to index.article_key, "describe" to index.describe, "type" to (type ?: 0))
            }
        }
    }

    /**
     * 查询数据
     */
    fun select(type: Int, result: (List<ArticleKeyMode>) -> Unit) {
        MySqlHelper.getInstance(BaseApplication.mApplication!!.get()!!).use {
            result(select(MySqlHelper.PDCHANNEL).whereArgs("type=$type").parseList(classParser()))
        }
    }
}