package com.wj.commonlib.http


/**
 * 接口管理
 * @author wangjun
 * @version 1.0
 * @date 2018/6/2
 */
object Urls {
    var DEBUG=false

    var API_VERSION="v1"
    /**
     * 域名
     */
    private val HEADER by lazy {
        if(DEBUG) "http://192.168.0.109" else "http://api.jzzb.info"//192.168.199.230 192.168.0.109
    }

    /**
     * 论坛地址
     */
    val DZURL by lazy {
        if(DEBUG) "https://bbs.jzzb.info" else "https://bbs.jzzb.info"
    }


    /**初始化*/
    val init="$HEADER/app/init"
    /**升级*/
    val update="$HEADER/app/update"
    /**登录*/
    val login="$HEADER/app/loginOther"
    /**退出登录*/
    val loginOut="$HEADER/app/loginOut"
    /**用户详情*/
    val userdetail="$HEADER/app/userDetail"
    /**用户详情*/
    val changeUserDetail="$HEADER/app/changUserInfo"
    /**获取广告详情详情*/
    val addetail="$HEADER/app/ad"
    /**文章列表*/
    val articleList="$HEADER/app/article/list"
    /**文章点赞*/
    val articleLike="$HEADER/app/article/like"
    /**文章阅读*/
    val articleRead="$HEADER/app/article/read"
    /**文章详情*/
    val articleDetail="$HEADER/app/article/detail"
    /**视频列表*/
    val videoList="$HEADER/app/video/list"
    /**视频点赞*/
    val videoLike="$HEADER/app/video/like"
    /**视频阅读*/
    val videoRead="$HEADER/app/video/read"
    /**vip解析接口列表*/
    val vipParsList="$HEADER/app/vip/list"
    /**用户协议*/
    val userRule="$HEADER/rule"
    /**用户隐私协议*/
    val userPrivacyRule="$HEADER/privacy"
    /**app协议提示*/
    val appRuleTips="$HEADER/app/appRuleTips"
    /**任务接单提示*/
    val taskRule="$HEADER/taskRule"
    /**发现*/
    val found="$HEADER/app/found"
    /**文章分享的链接*/
    val share_art="$HEADER/newDetail?id="
    /**客服的qq*/
    val customer_service="$HEADER/app/customerService"
    /**客服的qq*/
    val get_uc_token="$HEADER/app/uc/token"
    /**获取有米任务完成没有*/
    val ym_task="$HEADER/app/ymTask"
    /**消息公告的链接*/
    val announcement="$HEADER/announcement?id="
    /**积分规则说明*/
    val signRule="$HEADER/signRule"
    /**提现的规则*/
    val withdrawalRule="$HEADER/txRule"
    /**签到*/
    val sign="$HEADER/app/sign"
    /**签到信息*/
    val signInfo="$HEADER/app/signInfo"
    /**福利列表*/
    val welfareList="$HEADER/app/welfare"
    /**可以使用的福利列表*/
    val welfareUseList="$HEADER/app/welfareUseList"
    /**领取福利*/
    val getWelfare="$HEADER/app/getWelfare"
    /**提现的产品列表*/
    val exchangeProjects="$HEADER/app/productList"
    /**提现的申请列表*/
    val withdrawalList="$HEADER/app/withdrawalList"
    /**绑定用户的邀请码*/
    val bindUserCode="$HEADER/app/bindUserCode"
    /**抽奖产品列表*/
    val luckyList="$HEADER/app/luckyList"
    /**抽奖产品列表*/
    val luckyResult="$HEADER/app/luckyResult"
    /**今天可以抽奖次数*/
    val luckyCount="$HEADER/app/luckyCount"
    /**抽奖记录*/
    val luckyRecode="$HEADER/app/luckyRecode"
    /**收支明细*/
    val billList="$HEADER/app/billList"
    /**获取支付信息*/
    val paymentInfo="$HEADER/app/paymentInfo"
    /**申请提现*/
    val withdrawal="$HEADER/app/withdrawal"
    /**绑定提现的信息*/
    val bindPaymentInfo="$HEADER/app/bindPaymentInfo"
    /**修改绑定提现的信息*/
    val changePaymentInfo="$HEADER/app/changePaymentInfo"
    /**首页所有的工具信息*/
    val homeToolAllList="$HEADER/app/homeToolAllList"
    /**游戏配置信息*/
    val game="$HEADER/app/game"
    /**任务信息配置*/
    val taskInfo="$HEADER/app/taskInfo"
    /**文章任务返奖*/
    val artTask="$HEADER/app/readTask"
    /**分享任务返奖*/
    val shareTask="$HEADER/app/shareTask"
    /**视频任务返奖*/
    val videoTask="$HEADER/app/videoTask"
    /**视频任务返奖*/
    val announcements="$HEADER/app/announcements"
    /**查询用户绑定的列表*/
    val selectHasBind="$HEADER/app/selectHasBind"
    /**使用教程*/
    val userGuide="$HEADER/appGuide/?id="
    /**上传接口*/
    val upLoad="$HEADER/upload"
    /**登录接口*/
    val pwLogin="$HEADER/user/login"
    /**注册接口*/
    val userRegister="$HEADER/app/reg"
    /**修改密码*/
    val changePwd="$HEADER/app/changePwd"
    /**发送验证码*/
    val sendSms="$HEADER/app/sendSms"
    /**绑定微信*/
    val bindWeChat="$HEADER/app/bindWeChat"
    /**绑定手机号*/
    val bindPhone="$HEADER/app/bindPhone"

    /**任务大厅*/
    val taskHall="$HEADER/app/hall/list"
    /**任务分类*/
    val taskTypeHall="$HEADER/app/hall/type"
    /**领取任务*/
    val getTask="$HEADER/app/getTask"
    /**查询任务列表*/
    val selectTask="$HEADER/app/selectTask"
    /**提交任务审核*/
    val submitTask="$HEADER/app/submitTask"
    /**查询任务状态*/
    val taskStatus="$HEADER/app/taskStatus"
    /**取消任务*/
    val taskCancel="$HEADER/app/taskCancel"

    /**支付宝支付*/
    val aliPay="$HEADER/app/payMoney"
    /**获取会员分类列表*/
    val vipCardList="$HEADER/app/vipCardList"


    /**第三方的影视接口 目前暂时用不自己部署和采集*/
    var movie_home="https://www.123ku.com"//例如http://www.kuyunzy1.com/list/?0-3.html
    val movie_list="$movie_home/?m=vod-index-pg-%s.html"//列表例子 http://www.kuyunzy1.com/list/?0-3.html 0是地区分类 3是页码
    val movie_type_list="$movie_home/?%s"
    val movie_search="$movie_home/index.php?m=vod-search-pg-%s-wd-%s.html"//搜索

    /**UC新闻token*/
    val uc_token="https://open.uczzd.cn/openiflow/auth/token"
    /**UC频道列表*/
    val uc_list="https://open.uczzd.cn/openiflow/openapi/v3/channels"
    /**UC频道的列表*/
    val uc_pd_list="https://open.uczzd.cn/openiflow/openapi/v3/channel/"
    /**UC本地频道的列表*/
    val uc_local_list="https://open.uczzd.cn/openiflow/openapi/v3/cities"
    /**UC文章分享的链接*/
    val uc_share="$HEADER/ucDetail?url="

    /**dz论坛_注册*/
    val dz_reg="$DZURL/api/register"
    /**dz论坛_登录*/
    val dz_login="$DZURL/api/login"
    /**dz论坛_分类*/
    val dz_categories="$DZURL/api/categories?createThread=1"
    /**dz论坛_帖子列表*/
    val dz_threads="$DZURL/api/threads"


    private const val search="http://api.pingcc.cn"
    /**漫画第三方接口 （搜索）*/
    const val comicList="$search/cartoon/search/"
    /**漫画第三方接口 （详情）*/
    const val comicDetail="$search/cartoonChapter/search/"
    /**漫画第三方接口 （图片列表）*/
    const val comicImgsList="$search/cartoonContent/search/"

    /**小说第三方接口 （搜索）*/
    const val novelList="$search/fiction/search/"
    /**小说第三方接口 （详情）*/
    const val novelDetail="$search/fictionChapter/search/"
    /**小说第三方接口 （图片列表）*/
    const val novelDList="$search/fictionContent/search/"

    /**影视第三方接口 (列表）*/
    const val pccMovieList="$search/video/search/"
    /**影视第三方接口 (详情）*/
    const val pccMovieDetail=" $search/videoChapter/search/"

}