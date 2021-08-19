package com.wj.makebai.ui.fragment

import android.graphics.Point
import android.widget.FrameLayout
import com.abase.view.weight.RecyclerSpace
import com.qq.e.ads.banner2.UnifiedBannerADListener
import com.qq.e.ads.banner2.UnifiedBannerView
import com.qq.e.comm.util.AdError
import com.wj.commonlib.http.HttpManager
import com.wj.makebai.R
import com.wj.makebai.statice.StaticData
import com.wj.makebai.ui.activity.base.MakeBaseFragment
import com.wj.makebai.ui.adapter.FoundAdapter
import com.wj.makebai.ui.weight.NoData
import com.wj.makebai.ui.weight.VegaLayoutManager
import kotlinx.android.synthetic.main.fragment_found_layout.*

/**
 * 发现
 * @author dchain
 * @version 1.0
 * @date 2019/4/16
 */
class FoundFragment : MakeBaseFragment() {
    private var bv: UnifiedBannerView? = null
    override fun setContentView(): Int {
        super.setContentView()
        return R.layout.fragment_found_layout
    }

    override fun setPageName(): String {
        return this::class.java.simpleName
    }

    override fun init() {
        super.init()
        getBanner()!!.loadAD()
    }

    override fun onPause() {
        super.onPause()
        imgView.pauseAnimation()
    }

    private fun loadData() {
        noData.type=NoData.DataState.LOADING
        HttpManager.found ({
            recyclerView_found.layoutManager = VegaLayoutManager(activity!!)
            recyclerView_found.adapter = FoundAdapter(it)
            if(recyclerView_found.itemDecorationCount<1)recyclerView_found.addItemDecoration(RecyclerSpace(10))
            noData.type=NoData.DataState.GONE
        },fun(code, _, _){
            if(code==503){
                noData.type=NoData.DataState.REFRESH
            }else if(code==-1){
                noData.type= NoData.DataState.NO_NETWORK
            }
        })
    }
    /**
     * 加载bannner
     */
    private fun getBanner(): UnifiedBannerView? {
        if (bv != null) {
            bannerContainer.removeView(bv)
            bv!!.destroy()
        }
        bv = UnifiedBannerView(activity,  StaticData.SPLASH_BANNER_ID, object :
            UnifiedBannerADListener {
            override fun onADCloseOverlay() {
            }

            override fun onADExposure() {
            }

            override fun onADClosed() {
            }

            override fun onADLeftApplication() {
            }

            override fun onADOpenOverlay() {
            }

            override fun onNoAD(p0: AdError?) {
            }

            override fun onADReceive() {
            }

            override fun onADClicked() {
            }
        })
        // 不需要传递tags使用下面构造函数
        bannerContainer.addView(bv, getUnifiedBannerLayoutParams())
        return bv
    }
    /**
     * banner2.0规定banner宽高比应该为6.4:1 , 开发者可自行设置符合规定宽高比的具体宽度和高度值
     *
     * @return
     */
    private fun getUnifiedBannerLayoutParams(): FrameLayout.LayoutParams? {
        val screenSize = Point()
        activity!!.windowManager.defaultDisplay.getSize(screenSize)
        return FrameLayout.LayoutParams(screenSize.x, Math.round(screenSize.x / 6.4f))
    }
    override fun onStop() {
        super.onStop()
        imgView.pauseAnimation()
    }
    override fun onResume() {
        super.onResume()
        imgView!!.setAnimation("wave.json")
        if (!imgView.isAnimating) imgView.resumeAnimation()
        if (recyclerView_found != null && recyclerView_found.adapter == null) {
            loadData()
        }
    }
}