package com.wj.makebai.ui.weight.sticky

import android.view.View

/**
 * Created by cpf on 2018/1/16.
 *
 * 获取吸附View相关的信息
 */
interface StickyView {
    /**
     * 是否是吸附view
     * @param view
     * @return
     */
    fun isStickyView(view: View?): Boolean

    /**
     * 得到吸附view的itemType
     * @return
     */
    var stickViewType: Int
}