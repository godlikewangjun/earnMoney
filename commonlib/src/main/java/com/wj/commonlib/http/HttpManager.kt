package com.wj.commonlib.http

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Looper
import androidx.core.content.ContextCompat
import com.abase.okhttp.OhHttpClient
import com.abase.okhttp.OhHttpParams
import com.abase.okhttp.log.HttpLoggingInterceptor
import com.abase.util.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.umeng.commonsdk.debug.E
import com.wj.commonlib.BuildConfig
import com.wj.commonlib.app.BaseApplication
import com.wj.commonlib.data.configs.Configs
import com.wj.commonlib.data.mode.*
import com.wj.commonlib.statices.EventCodes
import com.wj.commonlib.statices.Info
import com.wj.commonlib.statices.Statics
import com.wj.commonlib.utils.AesUtils
import com.wj.eventbus.WjEventBus
import com.wj.ktutils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.Response
import org.json.JSONObject
import java.io.File


/**
 * 管理接口
 * @author wangjun
 * @version 1.0
 * @date 2018/6/2
 */
open class HttpManager {
    private class NetworkInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            var requests = chain.request()
            val builder = requests.newBuilder()

            //服务端自动会转成小写
            if (Statics.userMode != null) builder.header("token", Statics.userMode!!.token)
            builder.header("api-version", Urls.API_VERSION).header("AppCode", Configs.CODE)
                .header("Mobile", "android")
                .header(
                    "VersionCode",
                    Tools.getAppVersionCode(BaseApplication.mApplication!!.get()).toString()
                )
                .header("Channel", Configs.CHANNCODE)
//                .header("web", "true")//为了测试不加密时候的数据


            requests = builder.build()
            val result = chain.proceed(requests)
            ///202 需重新登录（未授权） 201 被封号（禁止）
            if (result.code == 201 || result.code == 202) {
                WjEventBus.getInit().post(EventCodes.HTTPERROR, 0)
            } else if (result.code == 500) {
                if (BuildConfig.DEBUG) WjEventBus.getInit().post(
                    EventCodes.SERVERERROR,
                    result.body!!.source().readUtf8()
                )
            }
            return result
        }
    }

    companion object {

        private val interceptor = NetworkInterceptor()

        /**
         * 初始化设置
         */
        private fun initOkHttp() {
            OhHttpClient.getInit().isJsonFromMat = true
//            if(OhHttpClient.getInit().getLogging()==null)OhHttpClient.getInit().setLogging(HttpLoggingInterceptor())
            if (!OhHttpClient.getInit().client.newBuilder().networkInterceptors().contains(
                    interceptor
                )
            )
                OhHttpClient.getInit().client =
                    OhHttpClient.getInit().client.newBuilder().addNetworkInterceptor(OhHttpClient.getInit().logging).addNetworkInterceptor(interceptor)
                        .build()
            if (OhHttpClient.getInit().logging.bodyParsing == null && BuildConfig.DEBUG) OhHttpClient.getInit()
                .logging.bodyParsing =
                HttpLoggingInterceptor.BodyParsing { p0 ->
                    if (p0.startsWith("{")) {
                        try {
                            val baseMode = GsonUtil.getGson().fromJson(p0, BaseMode::class.java)
                            if (baseMode.errorCode != -1 && baseMode.data != null) {
                                return@BodyParsing AbStrUtil.formatJson(
                                    AesUtils.AES_cbc_decrypt(
                                        baseMode.data as String,
                                        Info.key,
                                        Info.iv
                                    )
                                )
                            }
                        } catch (e: Exception) {
                        }
                    }
                    p0!!
                }
            AbLogUtil.D = BuildConfig.DEBUG
        }

        /**
         * 构建公共参数
         */
        fun getParams(json: String?): OhHttpParams {
            initOkHttp()
            val params = OhHttpParams()
            params.put(
                "deviceno",
                if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
                        BaseApplication.mApplication!!.get()!!,
                        Manifest.permission.READ_PHONE_STATE
                    )
                    && android.os.Build.VERSION.SDK_INT < 29
                ) {
                    AbAppUtil.getDeviceId(BaseApplication.mApplication!!.get())
                } else Statics.OAID
            )
            if (json != null)
                params.put(
                    "data",
                    AesUtils.AES_cbc_encrypt(
                        json,
                        Info.key,
                        Info.iv
                    )
                )
            return params
        }

        /**
         * 拼接成数组
         */
        fun <T> getObjectList(jsonString: String, cls: Class<T>): List<T> {
            val list = java.util.ArrayList<T>()
            try {
                val gson = Gson()
                val arry = JsonParser().parse(jsonString).asJsonArray
                for (jsonElement in arry) {
                    list.add(gson.fromJson(jsonElement, cls))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return list
        }

        /**
         * 解密得到返回结果
         */
        inline fun <reified T> getResult(
            result: String,
            noinline fail: ((BaseMode) -> Unit)?,
            crossinline listener: (T) -> Unit
        ) {
            threadResUlt(result, fail) {
                try {
                    when {
                        T::class.java == String::class.java -> listener(it as T)
                        it.isNull() -> fail?.invoke(BaseMode(0, "返回空值", null))
                        else -> {
                            val type = object : TypeToken<T>() {}.type
                            if (!it.isNull()) listener(GsonUtil.getGson().fromJson(it, type))
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    AbLogUtil.e(HttpManager::class.java, "错误： $result")
                }
            }
        }

        /**
         * 解密得到返回结果
         */
        inline fun <reified T> getResults(
            result: String,
            noinline fail: ((BaseMode) -> Unit)?,
            crossinline listener: (ArrayList<T>) -> Unit
        ) {
            threadResUlt(result, fail) {
                listener(getObjectList(it, T::class.java) as ArrayList<T>)
            }
        }

        /**
         * 线程处理json返回的结果
         */
        fun threadResUlt(
            result: String,
            fail: ((BaseMode) -> Unit)?,
            formatJson: (String) -> Unit
        ) {
//            AbLogUtil.e(HttpManager::class.java,result)
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val baseMode = GsonUtil.getGson().fromJson(result, BaseMode::class.java)
                    if (baseMode.errorCode == -1 && baseMode.data != null) {
                        var str = baseMode.data!!
                        if (!baseMode.data!!.startsWith("{")) str =
                            AesUtils.AES_cbc_decrypt(baseMode.data as String, Info.key, Info.iv)
                        AbLogUtil.d(HttpManager::class.java, AbStrUtil.formatJson(str))
//                        Platform.get().log(4, AbStrUtil.formatJson(str), null)
                        if (Looper.myLooper() == null) Looper.prepare()
                        BaseApplication.mApplication!!.get()!!.runOnUiThread { formatJson(str) }
                    } else if (baseMode.errorCode == -1 && baseMode.data == null) {//如果只有返回的结果，不需要解密，统一返回错误数据
                        if (Looper.myLooper() == null) Looper.prepare()
                        AbLogUtil.i("log", baseMode.message)
                        fail?.invoke(baseMode)
                        BaseApplication.mApplication!!.get()!!.runOnUiThread { formatJson("") }
                    } else if (baseMode.errorCode == 1) {//如果只有返回的结果，不需要解密，统一返回错误数据
                        AbLogUtil.i("log", baseMode.message)
                        fail?.invoke(baseMode)
                    } else {
                        AbLogUtil.e(HttpManager::class.java, result)
                        //参数错误需要弹出提示给用户
                        if (baseMode.errorCode == 0 && !baseMode.message!!.isNull()) {
                            if (Looper.myLooper() == null) Looper.prepare()
                            BaseApplication.mHandler.post {
                                fail?.invoke(baseMode)
                                BaseApplication.mApplication!!.get().showTip(baseMode.message!!)
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    fail?.invoke(BaseMode(-1, "解析错误"))
                }
            }
        }

        /**
         * 初始化的值
         */
        fun init(success_: (InitMode) -> Unit, fail_: (Int, String?, Throwable?) -> Unit) {
            Urls.init.post(getParams(null), success_, fail_)
        }

        /**
         * 检查升级
         */
        fun update(
            success_: (UpDataMode) -> Unit,
            fail_: (Int, String?, Throwable?) -> Unit,
            finish_: () -> Unit
        ) {
            val json = JsonObject()
            json.addProperty("channel", Configs.CHANNCODE)
            json.addProperty("platform", "android")
            json.addProperty("key", "app")
            json.addProperty(
                "version",
                Tools.getAppVersionCode(BaseApplication.mApplication!!.get())
            )
            Urls.update.post(getParams(json.toString()), success_, fail_, finish_)
        }

        /**
         * 插件升级
         */
        fun pluginUpdate(key: String, version: Int, success_: (UpDataMode) -> Unit) {

            val json = JsonObject()
            json.addProperty("channel", Configs.CHANNCODE)
            json.addProperty("key", key)
            json.addProperty("platform", "android")
            json.addProperty("version", version)
            Urls.update.post(getParams(json.toString()), success_)
        }

        /**
         * 下载升级
         */
        fun downLoadUpdate(success_: (UpDataMode) -> Unit) {
            val json = JsonObject()
            json.addProperty("ChannelCode", Configs.CHANNCODE)
            json.addProperty("Platform", 4)
            json.addProperty("VersionCode", 1)
            Urls.update.post(getParams(json.toString()), success_)
        }

        /**
         * 登录
         */
        @SuppressLint("MissingPermission")
        fun login(loginInfo: JsonObject, success_: (UserMode) -> Unit, finish_: () -> Unit) {
            http {
                url = Urls.login
                requestType = HttpRequests.POST
                ohhttpparams = getParams(loginInfo.toString())
                success = {
                    getResult<UserMode>(it as String, {}) {
                        Statics.userMode = it
                        success_(Statics.userMode!!)
                    }
                }
                finish = finish_
            }
        }

        /**
         * 退出登录
         */
        fun loginOut(finish_: () -> Unit) {
            Urls.loginOut.post<String>(getParams(null), finish_ = finish_)
        }

        /**
         * 获取用户详细资料
         */
        fun userDetail(success_: (UserMode) -> Unit, fail_: ((Int, String?, Throwable?) -> Unit)?) {
            Urls.userdetail.post(getParams(null), success_, fail_)
        }

        /**
         * 修改用户信息
         */
        fun changeUserInfo(
            nickName: String?,
            userIcon: String?,
            usersex: Int?,
            success_: (UserMode) -> Unit
        ) {
            val json = JsonObject()
            json.addProperty(
                "username",
                if (!nickName.isNull()) nickName else Statics.userMode!!.username
            )
            json.addProperty(
                "usericon",
                if (!userIcon.isNull()) userIcon else Statics.userMode!!.usericon
            )
            json.addProperty(
                "usersex",
                if (!userIcon.isNull()) usersex else Statics.userMode!!.usersex
            )
            Urls.changeUserDetail.post(getParams(json.toString()), success_)
        }

        /**
         * 获取广告类容
         */
        fun addetail(key: String, success_: (ArrayList<AdMode>) -> Unit) {
            val json = JsonObject()
            json.addProperty("adkey", key)
            Urls.addetail.post(getParams(json.toString()), success_)
        }


        /**
         * 获取文章详情 为了兼容网页唤醒
         */
        fun articleDetail(
            articleId: String, success_: (ArticleMode) -> Unit,
            fail_: ((Int, String?, Throwable?) -> Unit)
        ) {
            val json = JsonObject()
            json.addProperty("articleid", articleId)
            Urls.articleDetail.post(getParams(json.toString()), success_, fail_)
        }

        /**
         * 列表查询通用接口
         * 需要解决多重泛型嵌套gson识别不了的问题
         */
        fun <T> allList(
            apiUrl: String,
            page: Int,
            startId: Int,
            key: String?,
            search: String?,
            success_: (PageMode<T>) -> Unit,
            fail_: ((Int, String?, Throwable?) -> Unit)
        ) {
            val json = JsonObject()
            json.addProperty("page", page)
            json.addProperty("startId", startId)
            json.addProperty("limit", 10)
            if (key != null) json.addProperty("key", key)
            if (search != null) json.addProperty("search", search)
            if (Statics.userMode != null) json.addProperty("userId", Statics.userMode!!.userid)

            apiUrl.post(getParams(json.toString()), success_, fail_)
        }

        /**
         * 点赞和阅读
         * @type 0是视频阅读 1视频是点赞 2是文章阅读 3文章是点赞
         */
        fun likeOrRead(videoId: Int, type: Int, success_: (String) -> Unit) {
            val json = JsonObject()
            json.addProperty("id", videoId)
            (if (type == 0) Urls.videoRead else if (type == 1) Urls.videoLike else if (type == 2) Urls.articleRead else Urls.articleLike).post(
                getParams(json.toString()),
                success_,
                null,
                null
            )
        }

        /**
         * vip解析接口列表
         */
        fun vipUrlsList(success_: (ArrayList<VipUrlMode>) -> Unit) {
            Urls.vipParsList.post(getParams(null), success_)
        }

        /**
         * 搜索漫画
         */
        fun comicList(
            isSearch: Boolean,
            search: String,
            page: Int,
            success_: (ComicMode) -> Unit,
            fail_: (Int, String?, Throwable?) -> Unit
        ) {
            http {
                url = (if(isSearch)Urls.comicList+"title/" else Urls.comicList+"cartoonType/") + search + "/$page/10"
                requestType = HttpRequests.GET
                success = {
                    GlobalScope.launch(Dispatchers.IO) {
                        val result = GsonUtil.gson2Object(it as String, ComicMode::class.java)
                        BaseApplication.mHandler.post {
                            success_(result)
                        }
                    }
                }
                fail = fail_
            }
        }

        /**
         * 漫画详情
         */
        fun comicDetail(
            id: String,
            success_: (ComicDetailMode) -> Unit,
            fail_: (Int, String?, Throwable?) -> Unit
        ) {
            http {
                url = Urls.comicDetail + id
                requestType = HttpRequests.GET
                success = {
                    GlobalScope.launch(Dispatchers.IO) {
                        val result = GsonUtil.gson2Object(it as String, ComicDetailMode::class.java)
                        BaseApplication.mHandler.post {
                            success_(result)
                        }
                    }
                }
                fail = fail_
            }
        }

        /**
         * 漫画目录的图片集
         */
        fun comicImpsList(data: String, success_: (ComicImgsMode) -> Unit) {
            http {
                url = Urls.comicImgsList + data
                requestType = HttpRequests.GET
                success = {
                    GlobalScope.launch(Dispatchers.IO) {
                        val result = GsonUtil.gson2Object(it as String, ComicImgsMode::class.java)
                        BaseApplication.mHandler.post {
                            success_(result)
                        }
                    }
                }
                fail = fun(code, message, thrrow) {
                }
            }
        }

        /**
         * 搜索小说
         */
        fun novelList(
            isSearch: Boolean,
            search: String,
            page: Int,
            success_: (NovelMode) -> Unit,
            fail_: (Int, String?, Throwable?) -> Unit
        ) {
            http {
                url = (if (isSearch) Urls.novelList+"title/" else Urls.novelList+"fictionType/") + search + "/$page/10"
                requestType = HttpRequests.GET
                success = {
                    GlobalScope.launch(Dispatchers.IO) {
                        val result = GsonUtil.gson2Object(it as String, NovelMode::class.java)
                        BaseApplication.mHandler.post {
                            success_(result)
                        }
                    }
                }
                fail = fail_
            }
        }

        /**
         * 小说详情
         */
        fun novelDetail(
            id: String,
            success_: (NovelPageMode) -> Unit,
            fail_: (Int, String?, Throwable?) -> Unit
        ) {
            http {
                url = Urls.novelDetail + id
                requestType = HttpRequests.GET
                success = {
                    GlobalScope.launch(Dispatchers.IO) {
                        val result = GsonUtil.gson2Object(
                            it as String,
                            NovelPageMode::class.java
                        )
                        BaseApplication.mHandler.post {
                            success_(result)
                        }
                    }
                }
                fail = fail_
            }
        }

        /**
         * 小说内容
         */
        fun novelText(data: String, success_: (NovelContentMode) -> Unit) {
            http {
                url = Urls.novelDList + data
                requestType = HttpRequests.GET
                success = {
                    GlobalScope.launch(Dispatchers.IO) {
                        val result =
                            GsonUtil.gson2Object(it as String, NovelContentMode::class.java)
                        BaseApplication.mHandler.post {
                            success_(result)
                        }
                    }
                }
                fail = fun(code, message, thrrow) {
                }
            }
        }

        /**
         * app的协议内容提示
         */
        fun appRuleTips(success_: (ConfigMode) -> Unit) {
            Urls.appRuleTips.post(getParams(null), success_)
        }

        /**
         * 发现内容
         */
        fun found(
            success_: (ArrayList<FoundMode>) -> Unit,
            fail_: (Int, String?, Throwable?) -> Unit
        ) {
            Urls.found.post(getParams(null), success_, fail_)
        }

        /**
         * 搜索视频
         */
        fun vipMovieList(
            isSearch: Boolean,
            search: String,
            page: Int,
            success_: (VipMovieMode) -> Unit,
            fail_: (Int, String?, Throwable?) -> Unit
        ) {
            http {
                url = (if(isSearch) Urls.pccMovieList+"title/" else Urls.pccMovieList+"videoType/") + search + "/$page/10"
                requestType = HttpRequests.GET
                success = {
                    GlobalScope.launch(Dispatchers.IO) {
                        val result = GsonUtil.gson2Object(it as String, VipMovieMode::class.java)
                        BaseApplication.mHandler.post {
                            success_(result)
                        }
                    }
                }
                fail = fail_
            }
        }

        /**
         * vip视频详情
         */
        fun vipMovieDetail(url_link: String, success_: (VipParsMovieMode) -> Unit) {
            http {
                url = Urls.pccMovieDetail + url_link
                requestType = HttpRequests.GET
                success = {
                    GlobalScope.launch(Dispatchers.IO) {
                        val result =
                            GsonUtil.gson2Object(it as String, VipParsMovieMode::class.java)
                        BaseApplication.mHandler.post {
                            success_(result)
                        }
                    }
                }
            }
        }

        /**
         * 获取客服的QQ
         */
        fun customerServiceQQ(success_: (ArrayList<Announcement>) -> Unit) {
            Urls.customer_service.post(getParams(null), success_)
        }


        /**
         * 判断有米任务完成没有
         */
        fun youmiDetail(adId: Int, success_: (String) -> Unit) {
            val json = JsonObject()
            json.addProperty("adid", adId)
            Urls.ym_task.post(getParams(json.toString()), success_)
        }

        /**
         * app签到
         */
        fun sign(success_: (SignResultMode) -> Unit) {
            Urls.sign.post(getParams(null), success_)
        }

        /**
         * 签到信息
         */
        fun signInfo(success_: (SignInfoMode) -> Unit) {
            Urls.signInfo.post(getParams(null), success_)
        }

        /**
         * 福利列表
         */
        fun welfareList(success_: (ArrayList<WelfareMode>) -> Unit) {
            val json = JsonObject()
            if (Statics.userMode != null) json.addProperty("userId", Statics.userMode!!.userid)
            Urls.welfareList.post(
                getParams(if (Statics.userMode != null) json.toString() else null),
                success_
            )
        }

        /**
         * 可以使用的福利列表
         */
        fun welfareUseList(success_: (ArrayList<WelfareMode>) -> Unit) {
            val json = JsonObject()
            if (Statics.userMode != null) json.addProperty("userId", Statics.userMode!!.userid)
            Urls.welfareUseList.post(getParams(json.toString()), success_)
        }

        /**
         * 领取福利
         */
        fun getWelfare(welfareId: Int, success_: (String) -> Unit) {
            val json = JsonObject()
            json.addProperty("welfareId", welfareId)
            Urls.getWelfare.post(getParams(null), success_)
        }

        /**
         * 兑换的产品列表
         */
        fun exchangeProjects(success_: (ArrayList<ExChangProduct>) -> Unit) {
            Urls.exchangeProjects.post(getParams(null), success_)
        }

        /**
         * 申请提现的列表
         */
        fun withdrawList(page: Int?, success_: (ArrayList<WithdrawalMode>) -> Unit) {
            var json: JsonObject? = null
            if (page != null) {
                json = JsonObject()
                json.addProperty("page", page)
            }
            Urls.withdrawalList.post(getParams(json?.toString()), success_)
        }

        /**
         * 绑定邀请码
         */
        fun bindUserCode(userCode: String, success_: (UserMode) -> Unit) {
            val json = JsonObject()
            json.addProperty("userCode", userCode)
            Urls.bindUserCode.post(getParams(json.toString()), success_)
        }

        /**
         * 抽奖产品列表
         */
        fun luckyList(success_: (ArrayList<LuckyMode>) -> Unit) {
            Urls.luckyList.post(getParams(null), success_)
        }

        /**
         * 抽奖
         */
        fun luckyResult(success_: (Int) -> Unit, fail_: (Int, String?, Throwable?) -> Unit) {
            Urls.luckyResult.post(getParams(null), success_, fail_)
        }

        /**
         * 抽奖次数
         */
        fun luckyCount(success_: (Int) -> Unit) {
            Urls.luckyCount.post(getParams(null), success_)
        }

        /**
         * 文件上传
         */
        fun upload(file: ArrayList<File>, success_: (String) -> Unit) {
            httpfile {
                url = Urls.upLoad
                requestType = HttpFile.UPLOAD
                ohhttpparams = getParams(null)
                upFile = file
                success = {
                    getResult<String>(it as String, {}) {
                        success_(JSONObject(it).getString("imageurl"))
                    }
                }
            }
        }

        /**
         * 抽奖记录
         */
        fun luckyRecode(success_: (ArrayList<LuckyRecodeMode>) -> Unit) {
            Urls.luckyRecode.post(getParams(null), success_)
        }

        /**
         * 收支明细
         */
        fun billList(success_: (ArrayList<BillMode>) -> Unit) {
            Urls.billList.post(getParams(null), success_)
        }

        /**
         *获取支付信息看是否可以提现
         */
        fun paymentInfo(success_: (PaymentInfoMode) -> Unit) {
            Urls.paymentInfo.post(getParams(null), success_)
        }

        /**
         *申请提现
         */
        fun withdrawal(
            idwithdrawa: Int,
            welfareid: Int,
            success_: (String) -> Unit,
            finish_: () -> Unit
        ) {
            val json = JsonObject()
            json.addProperty("idwithdrawal_type", idwithdrawa)
            if (welfareid != -1) json.addProperty("welfareid", welfareid)
            Urls.withdrawal.post(getParams(json.toString()), success_, finish_ = finish_)
        }

        /**
         *绑定提现信息
         */
        fun bindPaymentInfo(wx_num: String, wx_phone: String, success_: (String) -> Unit) {
            val json = JsonObject()
            json.addProperty("usernum", wx_num)
            json.addProperty("phone", wx_phone)
            Urls.bindPaymentInfo.post(getParams(json.toString()), success_)
        }

        /**
         *修改绑定提现信息
         */
        fun changePaymentInfo(wx_num: String, wx_phone: String, success_: (String) -> Unit) {
            val json = JsonObject()
            json.addProperty("usernum", wx_num)
            json.addProperty("phone", wx_phone)
            Urls.changePaymentInfo.post(getParams(json.toString()), success_)
        }

        /**
         *所有的数据工具
         */
        fun homeToolAllList(success_: (ArrayList<HomeToolsMode>) -> Unit) {
            Urls.homeToolAllList.post(getParams(null), success_)
        }

        /**
         *游戏配置信息
         */
        fun gameInfo(success_: (ConfigMode) -> Unit) {
            Urls.game.post(getParams(null), success_)
        }

        /**
         * 任务信息配置
         */
        fun taskInfo(success_: (TaskConfigMode) -> Unit, finish_: () -> Unit) {
            Urls.taskInfo.post(getParams(null), success_, finish_ = finish_)
        }

        /**
         * 激励文章任务信息
         */
        fun artTask(
            art_id: Int,
            title: String,
            success_: (ArtTaskMode) -> Unit,
            finish_: () -> Unit
        ) {
            val json = JsonObject()
            json.addProperty("art_id", art_id)
            json.addProperty("title", title)
            Urls.artTask.post(getParams(json.toString()), success_, finish_ = finish_)
        }

        /**
         * 分享任务信息
         */
        fun shareTask(
            sharetype: Int,
            share_code: String,
            success_: (ArtTaskMode) -> Unit,
            finish_: () -> Unit
        ) {
            val json = JsonObject()
            json.addProperty("sharetype", sharetype)
            json.addProperty("share_code", share_code)
            Urls.shareTask.post(getParams(json.toString()), success_, finish_ = finish_)
        }

        /**
         * 视频观看
         */
        fun videoTask(success_: (ArtTaskMode) -> Unit, finish_: () -> Unit) {
            Urls.videoTask.post(getParams(null), success_, finish_ = finish_)
        }

        /**
         * 获取公告列表
         */
        fun announcements(
            startId: Int,
            type: Int?,
            success_: (ArrayList<Message>) -> Unit,
            finish_: () -> Unit
        ) {
            val json = JsonObject()
            if (Statics.userMode != null) json.addProperty("userId", Statics.userMode!!.userid)
            json.addProperty("pageSize", 10)
            json.addProperty("startId", startId)
            if (type != null) json.addProperty("type", type)
            Urls.announcements.post(getParams(json.toString()), success_, finish_ = finish_)
        }

        /**
         * 查询已经邀请的用户
         */
        fun selectHasBind(success_: (ArrayList<UserMode>) -> Unit, finish_: () -> Unit) {
            Urls.selectHasBind.post(getParams(null), success_, finish_ = finish_)
        }

        /**
         *账号密码的登录
         */
        fun pwLogin(
            userNum: String,
            password: String,
            success_: (UserMode) -> Unit,
            finish_: () -> Unit
        ) {
            val json = JsonObject()
            json.addProperty("usernum", userNum)
            json.addProperty(
                "password", AesUtils.AES_cbc_encrypt(
                    password,
                    Info.key,
                    Info.iv
                ).trim()
            )
            Urls.pwLogin.post(getParams(json.toString()), success_, finish_ = finish_)
        }

        /**
         *账号密码的注册
         */
        fun userRegister(
            userNum: String,
            password: String,
            phoneCode: String,
            success_: (UserMode) -> Unit,
            finish_: () -> Unit
        ) {
            val json = JsonObject()
            json.addProperty("phone", userNum)
            json.addProperty("userpwd", password)
            json.addProperty("phoneCode", phoneCode)
            Urls.userRegister.post(getParams(json.toString()), success_, finish_ = finish_)
        }

        /**
         *找回密码
         */
        fun findPwd(
            userNum: String,
            password: String,
            phoneCode: String,
            success_: (String) -> Unit,
            finish_: () -> Unit
        ) {
            val json = JsonObject()
            json.addProperty("phone", userNum)
            json.addProperty("userpwd", password)
            json.addProperty("phoneCode", phoneCode)
            Urls.changePwd.post(getParams(json.toString()), success_, finish_ = finish_)
        }

        /**
         *绑定手机
         */
        fun bindWeChat(
            json: String,
            success_: (String) -> Unit,
            finish_: () -> Unit
        ) {
            Urls.bindWeChat.post(getParams(json), success_, finish_ = finish_)
        }

        /**
         *绑定手机
         */
        fun bindPhone(
            userNum: String,
            phoneCode: String,
            success_: (String) -> Unit,
            finish_: () -> Unit
        ) {
            val json = JsonObject()
            json.addProperty("phoneNum", userNum)
            json.addProperty("phoneCode", phoneCode)
            Urls.bindPhone.post(getParams(json.toString()), success_, finish_ = finish_)
        }

        /**
         *发送验证码
         */
        fun sendSms(
            phoneNum: String,
            success_: (String) -> Unit,
            finish_: () -> Unit
        ) {
            val json = JsonObject()
            json.addProperty("phoneNum", phoneNum)
            Urls.sendSms.post(getParams(json.toString()), success_, finish_ = finish_)
        }
    }
}

inline fun <reified T> String.post(
    ohHttpParams: OhHttpParams?,
    noinline success_: ((T) -> Unit)? = {},
    noinline fail_: ((Int, String?, Throwable?) -> Unit)? = { _, _, _ -> },
    noinline finish_: (() -> Unit)? = {}
): String {
    httpMethod(HttpRequests.POST, ohHttpParams, success_, fail_, finish_)
    return this
}

inline fun <reified T> String.get(
    ohHttpParams: OhHttpParams?,
    noinline success_: ((T) -> Unit)? = {},
    noinline fail_: ((Int, String?, Throwable?) -> Unit)? = { _, _, _ -> },
    noinline finish_: (() -> Unit)? = {}
): String {
    httpMethod(HttpRequests.GET, ohHttpParams, success_, fail_, finish_)
    return this
}

inline fun <reified T> String.httpMethod(
    httpRequests: String,
    ohHttpParams: OhHttpParams?, noinline success_: ((T) -> Unit)?,
    noinline fail_: ((Int, String?, Throwable?) -> Unit)?, noinline finish_: (() -> Unit)?
) {
    val api = this
    http {
        url = api
        requestType = httpRequests
        if (ohHttpParams != null) ohhttpparams = ohHttpParams
        if (success_ != null) success = {
            HttpManager.getResult<T>(it as String, { try {
                fail_?.invoke(it.errorCode!!,it.message,null)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            }) {
                success_(it)
            }
        }
        if (fail_ != null) fail = fail_
        if (finish_ != null) finish = finish_
    }
}