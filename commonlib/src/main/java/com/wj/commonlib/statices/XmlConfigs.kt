package com.wj.commonlib.statices

/**
 *
 * @author Admin
 * @version 1.0
 * @date 2018/6/15
 */
object XmlConfigs {
    /**
     * appcode
     */
    val APPCODE = "appcode"

    /**
     * 默认的渠道号 优先取androidMainifest.xml的里面配置
     */
    val APPCHANNEl = "appchannel"

    /**
     * AccessToken
     */
    val ACCESSTOKEN = "accesstoken"

    /**
     * 用户信息
     */
    val USERMODE = "usermode"

    /**
     * 是否是首次进入
     */
    val ISFRIST = "isfrist"

    /**
     * 视频缓存
     */
    const val VIDEO_POSITION = "videoPosition"

    /**
     * 是否同意了服务协议
     */
    val ISAGRENN = "isagrenn"
    /**
     * 替代imei的oaid
     */
    val OAID = "oaid"
    /**缓存视频列表 防止没有数据的时候空白*/
    const val CACHE_VIDEOS="CACHE_VIDEOS"
    /**
     * 任务详情的引导是否展示
     */
    val G_TASKDETAIL = "g_taskdetail"
    /**
     * 论坛的指示
     */
    val G_BBS = "g_bbs"
    /**
     * 做任务的提示
     */
    val TASKSTART = "taskstart"

    /**dz的登录信息*/
    const val DZ_INFO="dz_info"

    /**uc的token*/
    const val UC_TOKEN="uc_token"
    /**uc的城市*/
    const val UC_CITYCODES="uc_citycodes"
}