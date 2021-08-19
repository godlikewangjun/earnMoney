package com.wj.commonlib.http

import com.abase.okhttp.OhHttpClient
import com.abase.okhttp.OhHttpParams
import com.abase.util.GsonUtil
import com.wj.commonlib.data.mode.DZListMode
import com.wj.commonlib.data.mode.UserMode
import com.wj.commonlib.data.mode.dz.DZCategories
import com.wj.commonlib.data.mode.dz.DZUserLogin
import com.wj.commonlib.data.mode.uc.UCpdMode
import com.wj.commonlib.statices.Statics
import com.wj.ktutils.HttpRequests
import com.wj.ktutils.http
import com.wj.ktutils.isNull
import com.xiaomi.push.it
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * 地址论坛的API接口
 * @author admin
 * @version 1.0
 * @date 2020/11/24
 */
object DzApi {
    private fun getHttpParams(json:JSONObject):OhHttpParams{
        val jsonObject=JSONObject()
        jsonObject.put("data",json)
        return  OhHttpParams().put(OhHttpClient.JSONTYE,jsonObject.toString())
    }
    /**
     * 注册用户 账号密码
     * https://discuz.com/api-docs/v1/register.html
     */
    fun dzReg(userMode: UserMode){
        val jsonObject=JSONObject()
        jsonObject.put("type","users")
        val attributes=JSONObject()
        attributes.put("username",if(userMode. user_phone.isNull())userMode.username else userMode. user_phone)
        attributes.put("password",userMode.userPwd)
        attributes.put("register_reason","app同步注册")
        jsonObject.put("attributes",attributes)
        http {
            url = Urls.dz_reg
            requestType = HttpRequests.POST
            ohhttpparams= getHttpParams(jsonObject)
            success = {
            }
            fail = fun(_, _, _) {
            }
        }
    }

    /**
     * 登录
     * https://discuz.com/api-docs/v1/register.html
     */
    fun dzLogin(userMode: UserMode, success_: (DZUserLogin) -> Unit){
        val jsonObject=JSONObject()
        val attributes=JSONObject()
        attributes.put("username",userMode.user_phone)
        attributes.put("password","q123456")
        jsonObject.put("attributes",attributes)
        http {
            url = Urls.dz_login
            requestType = HttpRequests.POST
            ohhttpparams= getHttpParams(jsonObject)
            success = {
                GlobalScope.launch(Dispatchers.IO) {
                    val json=GsonUtil.gson2Object(it as String,DZUserLogin::class.java)
                    launch(Dispatchers.Main){
                        Statics.DZ_LOGIN_INFO=json
                        success_(json)
                    }
                }
            }
            fail = fun(_, _, _) {
            }
        }
    }

    /**
     * 获取分类
     */
    fun dzCategories(success_: (DZCategories) -> Unit){
        http {
            url = Urls.dz_categories
            requestType = HttpRequests.GET
            success = {
                GlobalScope.launch(Dispatchers.IO) {
                    val json=GsonUtil.gson2Object(it as String, DZCategories::class.java)
                    launch(Dispatchers.Main){
                        success_(json)
                    }
                }
            }
            fail = fun(_, _, _) {
            }
        }
    }

    /**
     * 获取帖子列表
     */
    fun dzThreads(filter:String,success_: (DZListMode) -> Unit,  finish_: () -> Unit){
        http {
            url = Urls.dz_threads+filter
            requestType = HttpRequests.GET
            success = {
                GlobalScope.launch(Dispatchers.IO) {
                    val json=GsonUtil.gson2Object(it as String, DZListMode::class.java)
                    launch(Dispatchers.Main){
                        success_(json)
                    }
                }
            }
            finish=finish_
        }
    }
}