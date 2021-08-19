package com.wj.makebai.ui.weight

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.lifecycle.Lifecycle
import com.wj.makebai.R
import com.wj.makebai.ui.activity.parsing.AdMovieViewMode

/**
 *
 * @author admin
 * @version 1.0
 * @date 2021/2/7
 */
class AdVideoView:FrameLayout {
    constructor(context: Context) : super(context){init()}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs){init()}
    private var viewMode:AdMovieViewMode?=null
    private fun init(){
        val adView=LayoutInflater.from(context).inflate(R.layout.advideoview_layout,null)
        addView(adView)
    }
    fun loadAd(lifecycle: Lifecycle,play:()->Unit){
        if(viewMode==null) {
            viewMode=AdMovieViewMode()
            viewMode!!.apply {
                viewMode!!.init(context,this@AdVideoView,lifecycle)
                onVideoCompleted= play
            }
        }else viewMode!!.loadAd()
    }
}