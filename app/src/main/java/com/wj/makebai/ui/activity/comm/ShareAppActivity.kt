package com.wj.makebai.ui.activity.comm

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.view.View
import cn.bertsir.zbar.utils.QRUtils
import com.abase.util.AbFileUtil
import com.abase.util.Tools
import com.gyf.immersionbar.ImmersionBar
import com.umeng.socialize.ShareAction
import com.umeng.socialize.UMShareListener
import com.umeng.socialize.bean.SHARE_MEDIA
import com.umeng.socialize.media.UMImage
import com.umeng.socialize.media.UMWeb
import com.wj.commonlib.http.HttpManager
import com.wj.commonlib.statices.Statics
import com.wj.commonlib.utils.CommTools
import com.wj.ktutils.isNull
import com.wj.ktutils.showTip
import com.wj.ktutils.startActivity
import com.wj.makebai.R
import com.wj.makebai.statice.StaticData
import com.wj.makebai.ui.activity.base.MakeActivity
import com.wj.makebai.utils.MbTools
import kotlinx.android.synthetic.main.activity_shareapp.*
import java.io.File

/**
 * app推荐页面
 * @author Administrator
 * @version 1.0
 * @date 2019/11/19
 */
class ShareAppActivity : MakeActivity(), View.OnClickListener {
    private var qrCode: Bitmap? = null
    private var file:File?=null
    override fun bindLayout(): Int {
        return R.layout.activity_shareapp
    }

    override fun initData() {
        ImmersionBar.with(this).statusBarDarkFont(false).init()
        setThemeColor(R.color.blue1)
        backto_img.setImageResource(R.drawable.ic_back_w)
        title_line.visibility=View.GONE

        qrCode = QRUtils.getInstance().createQRCodeAddLogo(
            StaticData.appShareInfo!!.downLoad,
            BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
        )
        scan_logo.setImageBitmap(qrCode)
        if (Statics.userMode != null) {
            HttpManager.userDetail({
                user_code.text = Statics.userMode!!.userCode
                share_count.text = Statics.userMode!!.shareCount.toString()
                share_points.text = Statics.userMode!!.sharePoints.toString()
                if(!Statics.userMode!!.userPushId.isNull()){
                    user_bind_code.isEnabled=false
                    user_bind_code.setText(String.format(getString(R.string.has_bind),Statics.userMode!!.userPushId))
                    bind.visibility=View.GONE
                }
                user_bind_code.setTextColor(resources.getColor(R.color.text_gray))
            },null)
            other.setTextColor(resources.getColor(R.color.white))
            other.text=getString(R.string.has_bind_list)
        } else {
            tips.text = "登录后邀请才能获得奖励哦"
            copy.visibility = View.GONE
        }

        share.setOnClickListener(this)
        copy.setOnClickListener(this)
        bind.setOnClickListener(this)
        other_down.setOnClickListener(this)
        scan_logo.setOnLongClickListener {
            MbTools.saveBmp2Gallery(activity, qrCode, getString(R.string.app_name))
            showTip("保存成功")
            return@setOnLongClickListener true
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.copy -> {//复制
                CommTools.copy(activity, user_code.text.toString())
            }
            R.id.other_down->{//查看邀请的用户列表
                startActivity<HasBindListActivity>()
            }
            R.id.bind -> {//
                if (Statics.userMode == null) {
                    return showTip("请先登录")
                }
                if (user_bind_code.text.toString().isNull()) {
                    return showTip("邀请码不能为空")
                }
                HttpManager.bindUserCode(user_bind_code.text.toString()) {
                    showTip("绑定成功")
                    user_bind_code.isEnabled = false
                }
            }
            R.id.share -> {//分享
                val galleryPath = (Environment.getExternalStorageDirectory().toString()
                        + File.separator + Environment.DIRECTORY_DCIM
                        + File.separator + "Camera" + File.separator)
                file = File(galleryPath, "${getString(R.string.app_name)}.jpg")
                if (file!!.exists()) {
                    val web = UMImage(activity, qrCode)
                    web.setThumb(web)
                    try {
                        ShareAction(activity!!).withText("${StaticData.appShareInfo!!.slogan}.邀请码： " + Statics.userMode!!.userCode)
                            .withMedia(web).setDisplayList(
                            SHARE_MEDIA.WEIXIN,
                            SHARE_MEDIA.WEIXIN_CIRCLE,
                            SHARE_MEDIA.QQ,
                            SHARE_MEDIA.QZONE,
                        ).setCallback(object : UMShareListener {
                            override fun onCancel(p0: SHARE_MEDIA?) {

                            }

                            override fun onError(p0: SHARE_MEDIA?, p1: Throwable?) {
                            }

                            override fun onStart(p0: SHARE_MEDIA?) {
                            }

                            override fun onResult(p0: SHARE_MEDIA?) {
                            }

                        }).open()
                    } catch (e: Exception) {
                    }
                } else {
                    MbTools.shareDialog(activity){
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        if(file!=null)AbFileUtil.deleteFile(file)
        super.onDestroy()
    }
}