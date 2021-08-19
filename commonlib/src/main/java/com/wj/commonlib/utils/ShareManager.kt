package com.wj.commonlib.utils

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import com.abase.util.AbAppUtil
import com.umeng.socialize.ShareAction
import com.umeng.socialize.UMAuthListener
import com.umeng.socialize.UMShareAPI
import com.umeng.socialize.UMShareListener
import com.umeng.socialize.bean.SHARE_MEDIA
import com.umeng.socialize.media.UMImage
import com.umeng.socialize.media.UMWeb
import com.wj.ktutils.showTip
import java.io.ByteArrayOutputStream






/**
 * 分享管理类
 * @author Admin
 * @version 1.0
 * @date 2018/3/30
 */
class ShareManager {

    companion object {
        var shareManager: ShareManager? = null
            get() {
                if (field == null) {
                    field = ShareManager()
                }
                return field
            }
    }


    private var runnable: Runnable? = null
    private val handler = Handler()

    fun login(context: Activity,type:SHARE_MEDIA,listener: UMAuthListener){
        if(!AbAppUtil.isInstallApk(context.baseContext,"com.tencent.mm")){
            return context.showTip("没有安装微信")
        }
        UMShareAPI.get(context).getPlatformInfo(context,type, listener)
    }

    /**
     * 链接分享
     */
    fun share(context: Activity,type:SHARE_MEDIA,url:String,title:String,img:String?,content:String,listener: UMShareListener){
        if(!AbAppUtil.isInstallApk(context,"com.tencent.mm") && type.ordinal in 9..11){
            return context.showTip("没有安装微信")
        }else if(!AbAppUtil.isInstallApk(context,"com.tencent.mobileqq") && type.ordinal in 6..7){
            return context.showTip("没有安装QQ")
        }

        val web=UMWeb(url)
        web.title = title//标题
        if(img!=null)web.setThumb(UMImage(context,img))  //缩略图
        web.description = content//描述

        ShareAction(context)
            .setPlatform(type)//传入平台
            .withMedia(web)
            .setCallback(listener)//回调监听器
            .share()
    }

    private fun compress(bitmap: Bitmap): Bitmap {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        try {
            byteArrayOutputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }


    /**
     * 销毁
     */
    fun destory(context: Context) {
        if (runnable != null) {
            handler.removeCallbacks(runnable)
            runnable = null
        }
    }

    interface ShareListener {
        fun success()
        fun fail()
    }
}