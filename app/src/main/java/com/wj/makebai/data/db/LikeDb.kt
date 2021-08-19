package com.wj.makebai.data.db

import android.content.Context
import com.wj.ktutils.db.classParser
import com.wj.ktutils.db.delete
import com.wj.ktutils.db.insert
import com.wj.ktutils.db.select
import com.wj.makebai.data.mode.db.ReadHistoryMode

/**
 * 喜欢收藏的记录
 * @author dchain
 * @version 1.0
 * @date 2019/9/20
 */
object LikeDb {

    /**
     * 新增阅读记录
     */
    fun insert(context: Context, title: String, type: TypesEnum, value: String?) {
        context.database.use {
            delete(MySqlHelper.LIKE_TABLE, "title='$title' and type=" + type.ordinal)
            insert(
                MySqlHelper.LIKE_TABLE,
                "title" to title,
                "type" to type.ordinal,
                "value" to value
            )
        }
    }

    /**
     * 查询
     */
    fun query(context: Context, title: String, type: TypesEnum, list: (ReadHistoryMode) -> Unit) {
        context.database.use {
            val select =
                select(MySqlHelper.LIKE_TABLE).whereArgs("title='$title' and type=" + type.ordinal)
                    .parseOpt(
                        classParser<ReadHistoryMode>()
                    )
            if (select != null) list.invoke(select)
        }
    }

    /**
     * 查询
     */
    fun query(context: Context, type: TypesEnum, list: (List<ReadHistoryMode>) -> Unit) {
        context.database.use {
            val select =
                select(MySqlHelper.LIKE_TABLE).whereArgs("type=" + type.ordinal).parseList(
                    classParser<ReadHistoryMode>()
                )
            list.invoke(select)
        }
    }

    /**
     * 删除
     */
    fun delete(context: Context, id: Int) {
        context.database.use {
            delete(MySqlHelper.LIKE_TABLE, "_id=$id")
            close()
        }
    }
    /**
     * 删除
     */
    fun delete(context: Context, type: TypesEnum, title: String) {
        context.database.use {
            delete(MySqlHelper.LIKE_TABLE, "title='$title' and type=${type.ordinal}")
            close()
        }
    }
    /**
     * 清空
     */
    fun clear(context: Context,type: TypesEnum) {
        context.database.use {
            delete(MySqlHelper.LIKE_TABLE, "type=${type.ordinal}")
            close()
        }
    }
}