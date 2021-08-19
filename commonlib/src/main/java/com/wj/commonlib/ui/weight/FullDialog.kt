package com.wj.commonlib.ui.weight

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog

/**
 * 全屏dialog
 * @author admin
 * @version 1.0
 * @date 2020/9/17
 */
public class FullDialog : AlertDialog {
    constructor(p0: Context, p1: Int) : super(p0, p1)

    override fun create() {
        super.create()
        //设置window背景，默认的背景会有Padding值，不能全屏。当然不一定要是透明，你可以设置其他背景，替换默认的背景即可。
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        //一定要在setContentView之后调用，否则无效
        window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    override fun onStart() {
        super.onStart()
        val uiOptions = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
        this.window!!.decorView.systemUiVisibility = uiOptions
    }
}