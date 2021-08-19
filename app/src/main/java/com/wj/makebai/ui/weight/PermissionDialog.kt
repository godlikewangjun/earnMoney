package com.wj.makebai.ui.weight

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.Gravity
import android.view.View
import com.abase.util.AbViewUtil
import com.wj.makebai.R

/**
 * Created by fanchen on 2017/11/3.
 */
class PermissionDialog : AlertDialog, View.OnClickListener {
    private var dialogView: View? = null
    private var clickListener: View.OnClickListener? = null

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, themeResId: Int) : super(context, themeResId) {
        init(context)
    }

    protected constructor(
        context: Context,
        cancelable: Boolean,
        cancelListener: DialogInterface.OnCancelListener?
    ) : super(context, cancelable, cancelListener) {
        init(context)
    }

    private fun init(context: Context) {
        dialogView = View.inflate(context, R.layout.dialog_permission_guide, null)
        setView(dialogView, 0, 0, 0, 0)
        dialogView!!.findViewById<View>(R.id.tv_open).setOnClickListener(this)
        setCancelable(false)
        setCanceledOnTouchOutside(false)
    }

    override fun show() {
        super.show()
        val window = window
        val lp = window!!.attributes
        lp.width = AbViewUtil.dp2px(context,300f)
        lp.gravity = Gravity.CENTER
        window.attributes = lp
    }

    fun setOnClickListener(clickListener: View.OnClickListener?): PermissionDialog {
        this.clickListener = clickListener
        return this
    }

    override fun onClick(v: View) {
        dismiss()
        if (clickListener != null) {
            clickListener!!.onClick(v)
        }
    }
}