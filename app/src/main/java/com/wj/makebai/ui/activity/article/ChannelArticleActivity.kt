package com.wj.makebai.ui.activity.article

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.wj.commonlib.data.mode.ArticleKeyMode
import com.wj.commonlib.statices.EventCodes
import com.wj.eventbus.WjEventBus
import com.wj.ktutils.startActivity
import com.wj.makebai.R
import com.wj.makebai.data.db.PdtDb
import com.wj.makebai.ui.activity.base.MakeActivity
import com.wj.makebai.ui.adapter.TabAdapter
import com.wj.makebai.ui.fragment.ContentFragment
import kotlinx.android.synthetic.main.activity_channel.*


/**
 * 文章的分类
 * @author dchain
 * @version 1.0
 * @date 2019/9/29
 */
class ChannelArticleActivity :MakeActivity(){
    private var channels = arrayListOf<ArticleKeyMode>()
    private var tabAdapter: TabAdapter?=null



    override fun bindLayout(): Int {
        return R.layout.activity_channel
    }

    override fun initData() {
        more.setOnClickListener { startActivity<ChannelActivity>() }

        title_content.text=getString(R.string.news)

        WjEventBus.getInit().subscribe(EventCodes.XSPD,Int::class.java){
            if(viewPager!=null) PdtDb.select(0) {
                if(channels!=it){
                    val f=supportFragmentManager.beginTransaction()
                    for (index in tabAdapter!!.fragments!!){
                        f.remove(index)
                    }
                    f.commitNowAllowingStateLoss()
                    channels= it as ArrayList<ArticleKeyMode>
                    setList()
                }
            }
        }
        tab.addOnTabSelectedListener(object :TabLayout.OnTabSelectedListener{
            override fun onTabReselected(p0: TabLayout.Tab?) {
            }

            override fun onTabUnselected(p0: TabLayout.Tab?) {
                p0!!.customView = null
            }

            override fun onTabSelected(p0: TabLayout.Tab?) {
                val textView = TextView(activity)
                textView.layoutParams=FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,FrameLayout.LayoutParams.WRAP_CONTENT)
                textView.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                textView.textSize=18f
                textView.gravity=Gravity.CENTER
                textView.setTextColor(resources.getColor(R.color.colorPrimary))
                textView.text = p0!!.text
                p0.customView = textView
            }

        })
    }

    override fun onResume() {
        super.onResume()
        if (!isFrist) {
            if(channels.isEmpty()) PdtDb.select(0) {
                channels.addAll(it)
                setList()
            }

        }
    }
    /**
     * 加载频道数据
     */
    private fun setList(){
        if(channels.isEmpty()) return
        val fragments = arrayListOf<Fragment>()
        val titles = arrayListOf<String>()
        var artFragment: ContentFragment
        var bundle: Bundle
        for (index in channels) {
            artFragment= ContentFragment()
            bundle = Bundle()
            bundle.putString("key", index.article_key)
            bundle.putBoolean("refresh", true)
            artFragment.arguments = bundle

            fragments.add(artFragment)
            titles.add(index.describe)
        }
        tabAdapter = TabAdapter(supportFragmentManager,
            FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, fragments.toTypedArray(),titles)
        viewPager.offscreenPageLimit = fragments.size
        viewPager.adapter = tabAdapter
        tab.setupWithViewPager(viewPager)
    }


    override fun onDestroy() {
        super.onDestroy()
        WjEventBus.getInit().remove(EventCodes.XSPD)
    }
}