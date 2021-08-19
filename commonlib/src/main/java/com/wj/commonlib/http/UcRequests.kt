package com.wj.commonlib.http

import com.abase.okhttp.OhHttpParams
import com.abase.util.*
import com.wj.commonlib.app.BaseApplication
import com.wj.commonlib.data.mode.uc.UCpdMode
import com.wj.commonlib.data.mode.uc.UcCityCodes
import com.wj.commonlib.data.mode.uc.UcListMode
import com.wj.commonlib.statices.Statics
import com.wj.ktutils.HttpRequests
import com.wj.ktutils.http
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * uc的请求处理
 * @author dchain
 * @version 1.0
 * @date 2019/10/23
 */
object UcRequests {
    /**
     * 构建公共参数
     */
    private fun getParams(): OhHttpParams {
        return OhHttpParams().put("app", "zanbei-iflow")
            .put(
                "dn",
                if (Statics.userMode == null) 2 else Statics.userMode!!.userid
            )
            .put("fr", "android")
            .put("ve", Tools.getAppVersionCode(BaseApplication.mApplication!!.get()))
            .put("imei",  if(android.os.Build.VERSION.SDK_INT < 29) AbAppUtil.getDeviceId(BaseApplication.mApplication!!.get()) else "")
            .put("oaid", Statics.OAID)
            .put("access_token", Statics.UC_TOKEN)
            .put(
                "nt",
                if (AbWifiUtil.isWifiConnectivity(BaseApplication.mApplication!!.get())) 1 else 2
            )
    }

    /**
     * UC token
     */
    fun ucToken(success_: (String) -> Unit) {
        http {
            url = Urls.get_uc_token
            requestType = HttpRequests.POST
            success = {
                HttpManager.getResult<String>(it as String, {
                    val ohHttpParams = OhHttpParams()
                    ohHttpParams.put("app_id", "1571743162")
                    ohHttpParams.put("app_secret", "3f8cc3efc880932c90b4a1c839632921")
                    http {
                        url = Urls.uc_token
                        ohhttpparams = ohHttpParams
                        requestType = HttpRequests.POST
                        success = {
                            ucToken(success)
                        }
                        fail = fun(_, _, _) {
                        }
                    }

                }) {
                    val jsonObject = JSONObject(it)
                    Statics.UC_TOKEN = jsonObject.getString("service_token")
                    success_(Statics.UC_TOKEN)

                }
            }
            fail = fun(_, _, _) {
            }
        }
    }

    /**
     * UC 频道列表
     */
    fun ucPdList(success_: (UCpdMode) -> Unit, fail_: (Boolean) -> Unit) {
        http {
            url = Urls.uc_list + getParams().paramString
            requestType = HttpRequests.GET
            success = {
                val json = JSONObject()
                json.put("data", GsonUtil.gson2String(it))
                json.put("time", System.currentTimeMillis())
                GlobalScope.launch(Dispatchers.IO) {
                    val ucPdMode = GsonUtil.gson2Object(it as String, UCpdMode::class.java)
                    when (ucPdMode.status) {
                        0 -> BaseApplication.mHandler.post { success_(ucPdMode) }
                        -1 -> {
                            ucToken {
                                ucPdList(success_, fail_)
                            }
                        }
                        else -> {
                            fail_.invoke(true)
                        }
                    }
                }
            }
            fail = fun(_, _, _) {
                fail_.invoke(true)
            }
        }
    }

    /**
     * UC 频道的新闻列表
     */
    fun ucPdNewsList(
        id: Long,
        city_code: String?,
        success_: (UcListMode) -> Unit,
        fail_: (Int, String?, Throwable?) -> Unit,
        finish_:()->Unit
    ) {
        http {
            url = Urls.uc_pd_list + id + getParams().paramString
            if (city_code != null) {
                url += "&city_code=$city_code"
            }
            requestType = HttpRequests.GET
            success = {
                GlobalScope.launch(Dispatchers.IO) {
                    val ucPdMode = GsonUtil.gson2Object(it as String, UcListMode::class.java)
                    when (ucPdMode.status) {
                        0 -> BaseApplication.mHandler.post { success_(ucPdMode) }
                        -1 -> {
                            ucToken {
                                ucPdNewsList(id, city_code, success_, fail_,finish_)
                            }
                        }
                        else -> {
                            AbLogUtil.d(this::class.java, "UC 出问题")
                        }
                    }
                }
            }
            fail = fail_
            finish=finish_
        }
    }

    /**
     * UC 本地频道的新闻列表
     */
    fun ucLocalNewsList(
        success_: (UcCityCodes) -> Unit,
        fail_: (Int, String?, Throwable?) -> Unit
    ) {
        http {
            url = Urls.uc_local_list + getParams().paramString
            requestType = HttpRequests.GET
            success = {
                GlobalScope.launch(Dispatchers.IO) {
                    val ucPdMode = GsonUtil.gson2Object(it as String, UcCityCodes::class.java)
                    when (ucPdMode.status) {
                        0 -> BaseApplication.mHandler.post { success_(ucPdMode) }
                        -1 -> {
                            ucToken {
                                ucLocalNewsList(success_, fail_)
                            }
                        }
                        else -> {
                            AbLogUtil.d(this::class.java, "UC 出问题")
                        }
                    }
                }
            }
            fail = fail_
        }
    }

    /**
     * UC 广告展示打点
     */
    fun ucAdShow(linkUrl: String, fail_: (Int, String?, Throwable?) -> Unit) {
        http {
            url = linkUrl
            requestType = HttpRequests.GET
            fail = fail_
        }
    }
}