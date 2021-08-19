package com.wj.makebai.ui.weight.sticky

import android.view.View

/**
 * Created by cpf on 2018/1/16.
 */
class ExampleStickyView : StickyView {

    override fun isStickyView(view: View?): Boolean {
        return view!!.tag as Boolean
    }

    override var stickViewType: Int=-1
}