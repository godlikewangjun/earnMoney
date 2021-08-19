package com.wj.makebai.ui.activity.parsing

import android.graphics.PixelFormat
import android.graphics.Point
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import com.abase.view.weight.LoadWeb
import com.qq.e.ads.banner2.UnifiedBannerADListener
import com.qq.e.ads.banner2.UnifiedBannerView
import com.qq.e.comm.util.AdError
import com.wj.commonlib.data.mode.VipUrlMode
import com.wj.commonlib.http.HttpManager
import com.wj.eventbus.WjEventBus
import com.wj.ktutils.isNull
import com.wj.ktutils.startActivity
import com.wj.makebai.R
import com.wj.makebai.statice.StaticData
import com.wj.makebai.ui.activity.base.MakeActivity
import com.wj.makebai.ui.weight.NoData
import kotlinx.android.synthetic.main.activity_vip_layout.*

/**
 * vip解析接口
 * @author dchain
 * @version 1.0
 * @date 2019/9/24
 */
class VipActivity : MakeActivity(), View.OnClickListener {
    private var link=""
    private var searchLink=""
    private var list=ArrayList<VipUrlMode>()
    private var bv: UnifiedBannerView? = null
    override fun bindLayout(): Int {
        return R.layout.activity_vip_layout
    }

    override fun before() {
        super.before()
        window.setFormat(PixelFormat.TRANSLUCENT)
    }

    override fun initData() {
        title_content.text = getString(R.string.vip_parsing)

//        webView.settings.blockNetworkImage=true
        webView.settings.userAgentString="Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.134 Safari/537.36"
        setState(NoData.DataState.LOADING,false)

        //获取解析可用的接口
        HttpManager.vipUrlsList{
            list=it
            link=list[0].url
            spinner.adapter= ArrayAdapter(activity,android.R.layout.simple_spinner_dropdown_item,it.toName())


            setState(NoData.DataState.GONE,false)
        }

        ok.setOnClickListener(this)
        do_search.setOnClickListener(this)
        spinner.onItemSelectedListener=object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
                link=list[0].url
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                link=list[position].url
            }
        }
        refresh_layout.setOnRefreshListener {
            webView.reload()
            refresh_layout.finishRefresh()
        }
        getBanner()!!.loadAD()
    }

    /**
     * 加载banner
     */
    private fun getBanner(): UnifiedBannerView? {
        if (bv != null) {
            bannerContainer.removeView(bv)
            bv!!.destroy()
        }
        bv = UnifiedBannerView(this, StaticData.SPLASH_BANNER_ID, object :
            UnifiedBannerADListener{
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
        windowManager.defaultDisplay.getSize(screenSize)
        return FrameLayout.LayoutParams(screenSize.x, Math.round(screenSize.x / 6.4f))
    }
    /**
     * 转化
     */
    private fun ArrayList<VipUrlMode>.toName():ArrayList<String>{
        val strs=ArrayList<String>()
        for (index in this){
            strs.add(index.describe)
        }
        return strs
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.ok -> {
                vip_loading.setAnimation("dialog_loading.json")
                vip_loading.resumeAnimation()
                WjEventBus.getInit().subscribe(LoadWeb.LOADFINSH,String::class.java){
                    webView.visibility=View.VISIBLE

                    vip_loading.pauseAnimation()
                    vip_loading.cancelAnimation()
                    vip_loading.visibility=View.GONE
                }

                if(!url.text.isNull())webView.setUrl(link+url.text.toString())
                else webView.setUrl(link+"https://v.youku.com/v_show/id_XNDM3MTM3MDA4MA==.html?spm=a2ha1.12528442.m_4424_c_11054_1.d_2&s=6b50587e2b8e11e69c81&scm=20140719.rcmd.4424.show_6b50587e2b8e11e69c81")
            }
            R.id.do_search->{
                //直接跳转到网页
//                if(!searchLink.isNull())startActivity<WebActivity>("url" to searchLink+search_text.text.toString().trim())
                startActivity<VipMovieActivity>()
            }
        }
    }
    override fun onPause() {
        super.onPause()
        webView.performClick()
        webView.onPause()
        webView.pauseTimers()
    }


    override fun onResume() {
        super.onResume()
        webView.onResume()
        webView.resumeTimers()
    }
    override fun onStop() {
        webView.onPause()
        webView.pauseTimers()
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        WjEventBus.getInit().remove(LoadWeb.LOADFINSH)
        webView.removeAllViews()
        webView.destroy()
        if (bv != null) {
            bv!!.destroy()
        }
    }
}