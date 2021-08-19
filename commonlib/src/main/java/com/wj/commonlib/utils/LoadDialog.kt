package com.wj.commonlib.utils

import android.content.Context
import android.view.ViewGroup
import androidx.annotation.MainThread
import com.abase.util.AbViewUtil
import com.abase.view.weight.MyDialog
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.wj.commonlib.R
import java.lang.ref.WeakReference

/**
 * 加载dialog
 * @author Admin
 * @version 1.0
 * @date 2018/6/12
 */
object LoadDialog {
    private var dialogShow: WeakReference<MyDialog>? = null
    private var img: LottieAnimationView? = null
    @MainThread
    fun show(context: Context) {
        val dialog=MyDialog(context, R.style.transparent_dialog)
        dialogShow = WeakReference(dialog)
        img = LottieAnimationView(context)
        val wh = AbViewUtil.dp2px(context, 200f)
        img!!.layoutParams = ViewGroup.LayoutParams(wh, wh)
        img!!.setAnimation("dialog_loading.json")
        img!!.repeatMode = LottieDrawable.RESTART
        img!!.repeatCount = -1
        img!!.enableMergePathsForKitKatAndAbove(true)
        img!!.speed=1.2f
//        img!!.setBackgroundColor(Color.parseColor("#dedede"))
        dialog.setView(img)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()

        val window = dialog.window
        val lp = window!!.attributes
        lp.width =wh
        lp.height =wh
        window.attributes = lp
        img!!.playAnimation()
        dialog.setOnCancelListener {
            img?.cancelAnimation()
            img=null
        }
    }


    fun cancle() {
        if (dialogShow != null && dialogShow!!.get()!=null && dialogShow!!.get()!!.isShowing) {
            try {
                dialogShow!!.get()?.cancel()
            } catch (e: Exception) {
            }
        }
    }
}