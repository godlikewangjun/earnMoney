package com.wj.commonlib.data.mode.uc

/**
 * uc的频道类型
 * @author dchain
 * @version 1.0
 * @date 2019/10/23
 */
data class UCpdMode(
    val `data`: UCpdData,
    val extension: Extension,
    val message: String,
    val result: Result,
    val status: Int
)

data class UCpdData(
    val channel: List<UCpdChannel>
)

data class UCpdChannel(
    val bubbles: List<Any>,
    val color: String,
    val force_insert: Boolean,
    val force_insert_time: Int,
    val guide_mark_timestamp: Int,
    val guide_mark_type: Int,
    val guide_mark_val: String,
    val icon: String,
    val id: Long,
    val is_default: Boolean,
    val is_fixed: Boolean,
    val is_subscribed: Boolean,
    val name: String,
    val op_mark: String,
    val op_mark_etm: Int,
    val op_mark_stm: Int,
    val op_mark_type: String,
    val publish_strategy: Int,
    val repeat_count: Int,
    val status: Int,
    val sub_channel_style: Int,
    val sub_channels: List<Any>
)

class Extension(
)

data class Result(
    val message: String,
    val status: Int
)