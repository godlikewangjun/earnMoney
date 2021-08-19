package com.wj.makebai.data.mode.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 搜索历史的记录对象
 * @author Administrator
 * @version 1.0
 * @date 2019/9/2
 */
@Entity
data class SearchHistoryMode(@PrimaryKey(autoGenerate = true) val id: Int, val name: String, val type: Int)