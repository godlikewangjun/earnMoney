package com.wj.makebai.ui.weight

import android.animation.ObjectAnimator
import android.view.View

/**
 * @author dchain
 * @version 1.0
 * @date 2019/10/28
 */
class ScaleInAnimation @JvmOverloads constructor(private val mFrom: Float = 0.7f) {

    fun getAnimators(view: View): Array<ObjectAnimator> {
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", mFrom, 1f)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", mFrom, 1f)
        return arrayOf<ObjectAnimator>(scaleX, scaleY)
    }

}
