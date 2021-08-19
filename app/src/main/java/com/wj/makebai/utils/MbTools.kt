package com.wj.makebai.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.text.Html
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ForegroundColorSpan
import android.text.style.URLSpan
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import com.abase.util.Tools
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import com.umeng.analytics.MobclickAgent
import com.umeng.socialize.UMShareListener
import com.umeng.socialize.bean.SHARE_MEDIA
import com.wj.commonlib.http.HttpManager
import com.wj.commonlib.statices.Statics
import com.wj.commonlib.ui.ViewControl
import com.wj.commonlib.ui.weight.share.ShareDialog
import com.wj.commonlib.utils.ShareManager
import com.wj.ktutils.isNull
import com.wj.ktutils.showTip
import com.wj.ktutils.startActivity
import com.wj.makebai.R
import com.wj.makebai.statice.StaticData
import com.wj.makebai.ui.activity.appTask.EarnTaskActivity
import com.wj.makebai.ui.activity.article.ArticleDetailActivity
import com.wj.makebai.ui.activity.comm.WebActivity
import com.wj.makebai.ui.activity.user.LoginActivity
import kotlinx.android.synthetic.main.dialog_rules_layout.view.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


/**
 * 特定的项目工具
 * @author wangjun
 * @version 1.0
 * @date 2018/7/7
 */
object MbTools {
    fun ImageView.load(url:String){
        Glide.with(this).load(url).into(this)
    }
    fun ImageView.load(glide:RequestManager,url:String){
        glide.load(url).into(this)
    }
    fun ImageView.loadCover(glide:RequestManager,url:String){
        glide.load(url).apply(RequestOptions().placeholder(R.color.div1)).into(this)
    }

    fun getHtmlData(bodyHTML: String?): String {
        if (bodyHTML.isNull()) return ""
        val head =
            "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=no\"/>"
        return "<html>$head <style>{margin:0;padding:0;}img{max-width:100%;}</style><body>$bodyHTML</body></html>"
    }

    fun setHtml(context: Context, bodyHTML: String?, source: String, data: String): String {
        if (bodyHTML.isNull()) return ""
        val html = String(context.resources.assets.open("new_detail.html").readBytes())
        return html.replace("%s", bodyHTML!!, false).replace("{source}", source, false)
            .replace("{date}", data, false)
    }

    /**
     * 保存到相册
     */
    fun saveBmp2Gallery(context: Context, bmp: Bitmap?, picName: String) {
        var fileName: String? = null
        //系统相册目录
        val galleryPath = (Environment.getExternalStorageDirectory().toString()
                + File.separator + Environment.DIRECTORY_DCIM
                + File.separator + "Camera" + File.separator)

        // 声明文件对象
        var file: File? = null
        // 声明输出流
        var outStream: FileOutputStream? = null

        try {
            // 如果有目标文件，直接获得文件对象，否则创建一个以filename为名称的文件
            file = File(galleryPath, "$picName.jpg")

            // 获得文件相对路径
            fileName = file.toString()
            // 获得输出流，如果文件中有内容，追加内容
            outStream = FileOutputStream(fileName)
            bmp!!.compress(Bitmap.CompressFormat.JPEG, 90, outStream)

        } catch (e: Exception) {
            e.stackTrace
        } finally {
            try {
                outStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            MediaStore.Images.Media.insertImage(
                context.contentResolver,
                bmp, fileName, null
            )
            val values = ContentValues()
            values.put(MediaStore.Images.Media.DATA, galleryPath)
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            val uri = context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values
            )
            context.sendBroadcast(
                Intent(
                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                    Uri.parse("file://" + file!!.absolutePath)
                )
            )
        }
    }

    /**
     * 显示协议
     */
    @SuppressLint("SetTextI18n")
    fun showRules(activity: Context, html: String, result: (Boolean) -> Unit) {
        if (activity is Activity && (activity.isFinishing || activity.isDestroyed)) return
        val contentView =
            LayoutInflater.from(activity).inflate(R.layout.dialog_rules_layout, null)
        val dialog = ViewControl.customAlertDialog(activity, contentView, 300f)
        contentView.text_content.text = Html.fromHtml(html)

        openWebText(activity, contentView.text_content) { _, url ->
            activity.startActivity<WebActivity>("url" to url)
        }


        contentView.btn_cancel.setOnClickListener { dialog.cancel();result.invoke(false) }
        contentView.btn_update.setOnClickListener { dialog.cancel();result.invoke(true) }
    }

    /**
     * 打开网站服务条款
     */
    fun openWebText(
        activity: Context,
        textView: TextView,
        onClickListener: (Int, String) -> Unit
    ) {
        textView.movementMethod = LinkMovementMethod.getInstance()
        val text = textView.text
        if (text is Spannable) {
            val end = text.length
            val sp = textView.text as Spannable
            val urls = sp.getSpans(0, end, URLSpan::class.java)
            val style = SpannableStringBuilder(text)
            style.clearSpans() // should clear old spans
            for (index in urls.indices) {
                val url = urls[index]
                val myURLSpan = Tools.MyURLSpan(
                    url.url, activity,
                    null
                ) {
                    onClickListener.invoke(index, url.url)
                }
                style.setSpan(
                    myURLSpan, sp.getSpanStart(url),
                    sp.getSpanEnd(url), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                val redSpan = ForegroundColorSpan(Color.RED)
                style.setSpan(
                    redSpan, sp.getSpanStart(url),
                    sp.getSpanEnd(url), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            textView.text = style
        }
    }

    /**
     * 判断scheme Url是否可用
     */
    fun schemeValid(mContext: Context, url: String): Boolean {
        val manager = mContext.packageManager
        val action = Intent(Intent.ACTION_VIEW)
        action.data = Uri.parse(url)
        val list = manager.queryIntentActivities(action, PackageManager.GET_RESOLVED_FILTER)
        return list != null && list.size > 0
    }

    /**
     * 判断scheme跳转
     */
    fun schemeOpen(context: Context, scheme: String, schemeType: String) {
        if (!scheme.isNull()) {
            context.startActivity<ArticleDetailActivity>("id" to scheme)
        } else if (!schemeType.isNull()) {
            if (schemeType == "task") {
                context.startActivity<EarnTaskActivity>()
            }
        }
    }

    /**
     * 判断是否登录
     */
    fun isLogin(context: Context): Boolean {
        return if (Statics.userMode == null) {
            context.startActivity<LoginActivity>()
            context.showTip(context.getString(R.string.please_login))
            false
        } else {
            true
        }
    }
    fun String.urlCanLoad(): Boolean {
        return this.startsWith("http://") || this.startsWith("https://") ||
                this.startsWith("ftp://") || this.startsWith("file://")
    }

    /**
     * 分享
     */
    fun shareDialog(context: Activity,cancel:()->Unit){
        val shareDialog = ShareDialog(context)
        shareDialog.create()
        shareDialog.tv_weixin.setCompoundDrawablesWithIntrinsicBounds(
            0,
            R.drawable.ic_wx,
            0,
            0
        )
        shareDialog.tv_pyq.setCompoundDrawablesWithIntrinsicBounds(
            0,
            R.drawable.ic_pyq,
            0,
            0
        )
        shareDialog.tv_qq.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_qq, 0, 0)
        shareDialog.tv_qqhy.setCompoundDrawablesWithIntrinsicBounds(
            0,
            R.drawable.ic_qqzone,
            0,
            0
        )
        shareDialog.setOnItemClickListener {
            var type: SHARE_MEDIA = SHARE_MEDIA.WEIXIN
            var shareType = 0
            when (it.id) {
                R.id.tv_weixin -> {
                    type = SHARE_MEDIA.WEIXIN
                    shareType = 0
                }
                R.id.tv_pyq -> {
                    type = SHARE_MEDIA.WEIXIN_CIRCLE
                    shareType = 1
                }
                R.id.tv_qq -> {
                    type = SHARE_MEDIA.QQ
                    shareType = 2
                }
                R.id.tv_qqhy -> {
                    type = SHARE_MEDIA.QZONE
                    shareType = 3
                }
            }
            if (Statics.userMode == null) context.showTip("登录有奖励哦")
            else {
                val shareCode =
                    Tools.setMD5(Statics.userMode!!.userCode + shareType + (System.currentTimeMillis() / 1000).toString())
                ShareManager.shareManager!!.share(context, type,
                    StaticData.appShareInfo!!.downLoad + "?code=" + Statics.userMode!!.userCode + "&share_code=" + shareCode,
                    StaticData.appShareInfo!!.appname,
                    StaticData.appShareInfo!!.appicon,
                    StaticData.appShareInfo!!.describe, object : UMShareListener {
                        override fun onResult(p0: SHARE_MEDIA?) {
                            if (Statics.userMode != null) HttpManager.shareTask(
                                shareType,
                                shareCode,
                                {
                                    context.showTip("分享成功!")
                                }) {
                                shareDialog.cancel()
                            }
                        }

                        override fun onCancel(p0: SHARE_MEDIA?) {
                            shareDialog.cancel()
                        }

                        override fun onError(p0: SHARE_MEDIA?, p1: Throwable?) {
                            shareDialog.cancel()
                        }

                        override fun onStart(p0: SHARE_MEDIA?) {
                        }
                    })
            }
        }

        shareDialog.show()
        shareDialog.setOnCancelListener { cancel.invoke() }

    }
    /**
     * 判断是否登录
     */
    fun loginOut() {
        Statics.userMode = null
        //友盟统计
        MobclickAgent.onProfileSignOff()
        //腾讯IM退出
//        TUIKit.unInit()
    }
}