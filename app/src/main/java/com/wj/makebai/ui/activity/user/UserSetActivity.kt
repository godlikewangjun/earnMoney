package com.wj.makebai.ui.activity.user

import android.view.LayoutInflater
import android.view.View
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.wj.commonlib.http.HttpManager
import com.wj.commonlib.statices.Statics
import com.wj.commonlib.ui.ViewControl
import com.wj.commonlib.utils.CommTools
import com.wj.commonlib.utils.GlideEngine
import com.wj.ktutils.isNull
import com.wj.ktutils.showTip
import com.wj.ktutils.startActivity
import com.wj.makebai.R
import com.wj.makebai.ui.activity.base.MakeActivity
import kotlinx.android.synthetic.main.activity_userset.*
import kotlinx.android.synthetic.main.dialog_nicknamechange.view.*
import kotlinx.android.synthetic.main.dialog_wxbindchange.view.*
import kotlinx.android.synthetic.main.dialog_wxbindchange.view.bind
import kotlinx.android.synthetic.main.dialog_wxbindchange.view.close
import java.io.File

/**
 * 用户信息设置页面
 * @author Administrator
 * @version 1.0
 * @date 2019/12/2
 */
class UserSetActivity : MakeActivity(), View.OnClickListener {
    override fun bindLayout(): Int {
        return R.layout.activity_userset
    }

    override fun initData() {
        payment_bind.setOnClickListener(this)
        user_name.setOnClickListener(this)
        user_icon.setOnClickListener(this)
        user_bind.setOnClickListener(this)

        if (Statics.userMode!!.usertype == 2){
            if(!Statics.userMode!!.user_phone.isNull())user_bind_info.text=Statics.userMode!!.user_phone
            if(!Statics.userMode!!.otherid.isNull()) user_bind_info.text=Statics.userMode!!.otherid
        }
    }
    /**
     * 转化
     */
    private fun List<LocalMedia>.toFiles():ArrayList<File>{
        val files=ArrayList<File>()
        for (media in this){
            val path: String =  if(!media.androidQToPath.isNull()) media.androidQToPath else media.path
            files.add(File(path))
        }
        return files
    }
    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.user_bind->{//绑定
                if(!user_bind_info.text.isNull()) return showTip("已绑定")
                if (Statics.userMode!!.usertype == 2 && Statics.userMode!!.user_phone.isNull())
                    startActivity<BindPhoneActivity>()
                else if (Statics.userMode!!.usertype == 2 && Statics.userMode!!.otherid.isNull())
                    startActivity<BindWeChatActivity>()
            }
            R.id.user_name -> {//昵称修改
                val view =
                    LayoutInflater.from(activity).inflate(R.layout.dialog_nicknamechange, null)

                val dialog = ViewControl.customAlertDialog(activity, view, null)
                view.close.setOnClickListener {
                    dialog.cancel()
                }
                view.user_nickname.setText(Statics.userMode!!.username)
                view.bind.setOnClickListener {
                    if (view.user_nickname.text.isNull()) return@setOnClickListener showTip("昵称不能为空")
                    HttpManager.changeUserInfo(view.user_nickname.text.toString(), null, null) {
                        Statics.userMode = it
                        showTip("修改成功")
                        dialog.cancel()

//                        val map = HashMap<String, Any>()
//                        map[TIMUserProfile.TIM_PROFILE_TYPE_KEY_NICK] = Statics.userMode!!.username
//                        TIMFriendshipManager.getInstance().modifySelfProfile(map, null)
                    }
                }
            }
            R.id.user_icon -> {//头像修改
                PictureSelector.create(activity)
                    .openGallery(PictureMimeType.ofImage())
                    .compress(false)
                    .maxSelectNum(9)
                    .loadImageEngine(GlideEngine.createGlideEngine()) // 请参考Demo GlideEngine.java
                    .forResult { result ->
                        if(result.isNotEmpty()){
                            CommTools.zipImages(activity, result.toFiles(),object :
                                CommTools.ZipListener{
                                override fun zipImagesSuccess(zipImg: ArrayList<File>) {
                                    HttpManager.upload(zipImg){
                                        HttpManager.changeUserInfo(null,it,null){
                                            showTip("修改成功")
                                            Statics.userMode =it
//                                            val map = HashMap<String, Any>()
//                                            map[TIMUserProfile.TIM_PROFILE_TYPE_KEY_FACEURL] = Statics.userMode!!.usericon
//                                            TIMFriendshipManager.getInstance().modifySelfProfile(map, object :
//                                                TIMCallBack {
//                                                override fun onSuccess() {
//                                                }
//
//                                                override fun onError(p0: Int, p1: String?) {
//                                                }
//
//                                            })
                                        }
                                    }
                                }
                            })
                        }
                    }
            }
            R.id.payment_bind -> {//点击绑定
                HttpManager.paymentInfo { paymentInfoMode ->
                    val view =
                        LayoutInflater.from(activity).inflate(R.layout.dialog_wxbindchange, null)

                    val dialog = ViewControl.customAlertDialog(activity, view, null)
                    view.user_bind_wx.setText(paymentInfoMode.payment.usernum)
                    view.user_bind_phone.setText(paymentInfoMode.payment.phone)

                    view.close.setOnClickListener {
                        dialog.cancel()
                    }
                    view.bind.setOnClickListener {
                        if (!paymentInfoMode.isFlow) {
                            showTip("请先关注公众号")
                        } else {
                            when {
                                view.user_bind_wx.text.isNull() -> {
                                    showTip("请填写微信号")
                                }
                                view.user_bind_phone.text.isNull() -> {
                                    showTip("请填写手机号")
                                }
                                view.user_bind_phone.text?.length != 11 -> {
                                    showTip("请填写正确手机号")
                                }
                                else -> {
                                    HttpManager.changePaymentInfo(
                                        view.user_bind_wx.text.toString(),
                                        view.user_bind_phone.text.toString()
                                    ) {
                                        showTip("修改成功成功，可以提现了")
                                        dialog.cancel()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}