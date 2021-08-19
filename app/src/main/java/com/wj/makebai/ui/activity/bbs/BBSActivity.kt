package com.wj.makebai.ui.activity.bbs

import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.wj.commonlib.http.DzApi
import com.wj.commonlib.statices.Statics
import com.wj.makebai.R
import com.wj.makebai.ui.activity.base.MakeActivity
import com.wj.makebai.ui.adapter.home.HomeTabAdapter
import com.wj.makebai.ui.fragment.BBSListFragment
import kotlinx.android.synthetic.main.activity_bbs.*
import kotlinx.android.synthetic.main.activity_bbs.tab
import java.util.ArrayList

/**
 * 论坛首页
 * @author admin
 * @version 1.0
 * @date 2020/11/24
 */
class BBSActivity : MakeActivity() {
    override fun bindLayout(): Int {
        return R.layout.activity_bbs
    }

    override fun initData() {
        title.visibility = View.GONE
        if(Statics.DZ_LOGIN_INFO==null)
            DzApi.dzLogin(Statics.userMode!!){
                getData()
            }
        else getData()
    }
    private fun getData(){
        DzApi.dzCategories {
            if (it.data != null) {
                val fragments = ArrayList<Fragment>()
                tab.addTab(tab.newTab().setText("推荐"))
                fragments.add(BBSListFragment(""))
                for (index in it.data!!) {
                    tab.addTab(tab.newTab().setText(index.attributes.name))
                    fragments.add(BBSListFragment(index.id))
                }
                val tabAdapter = HomeTabAdapter(this, fragments)
                viewPager.offscreenPageLimit = fragments.size
                viewPager.adapter = tabAdapter
                viewPager.isSaveEnabled = false

                tab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                    override fun onTabSelected(tab: TabLayout.Tab?) {
                        viewPager.setCurrentItem(tab!!.position, true)
                    }

                    override fun onTabUnselected(tab: TabLayout.Tab?) {
                    }

                    override fun onTabReselected(tab: TabLayout.Tab?) {
                    }

                })
                viewPager.registerOnPageChangeCallback(object :ViewPager2.OnPageChangeCallback(){
                    override fun onPageSelected(position: Int) {
                        tab.getTabAt(position)?.select()
                    }
                })
            }
        }
    }
}