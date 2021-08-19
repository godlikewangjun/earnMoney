package com.wj.makebai.data.mode.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 阅读记录
 * @author dchain
 * @version 1.0
 * @date 2019/9/20
 */
@Entity
data class ReadHistoryMode(@PrimaryKey(autoGenerate = true) val id: Int, val type: Int, val title: String, val value:String)