package com.wj.makebai.ui.activity.article

import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import com.wj.commonlib.data.mode.uc.UCpdMode
import com.wj.commonlib.http.UcRequests
import com.wj.ktutils.startActivity
import com.wj.makebai.R
import com.wj.makebai.data.db.PdtDb
import com.wj.makebai.ui.activity.base.MakeActivity
import com.wj.makebai.ui.adapter.TabAdapter
import com.wj.makebai.ui.fragment.ContentFragment
import com.wj.makebai.ui.fragment.HomeFragment
import com.wj.makebai.ui.fragment.UcListFragment
import kotlinx.android.synthetic.main.activity_uclist.*

/**
 * uc的频道选择页面
 * @author Administrator
 * @version 1.0
 * @date 2020/4/21
 */
class UcChannelActivity : MakeActivity(), View.OnClickListener {
    private var adapter: TabAdapter? = null
    override fun bindLayout(): Int {
        return R.layout.activity_uclist
    }

    override fun initData() {
        title_content.text=getString(R.string.news)
//        more.setOnClickListener(this)
        viewpager.setScrollable(false)

        val success = fun(mode: UCpdMode) {
            val now = System.currentTimeMillis()
            if (adapter == null) {
                val fragments = arrayListOf<Fragment>()
                val titles = arrayListOf<String>()
                for (i in mode.data.channel) {
                    if (i.is_default) {
                        fragments.add(UcListFragment.newInstance(i.id))
                        titles.add(i.name)
                    }
                }

                adapter = TabAdapter(
                    supportFragmentManager,
                    FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,
                    fragments.toTypedArray(),
                    titles
                )
            }
//                   viewpager.offscreenPageLimit = mode.data.channel.size
            viewpager.adapter = adapter
            tabLayout.postDelayed({
                tabLayout.setupWithViewPager(
                    viewpager
                )
            }, 300)
        }
        if (HomeFragment.ucPd != null) {
            success.invoke(HomeFragment.ucPd!!)
        } else {
            //如果UC的不能用就用自己的
            UcRequests.ucPdList(success) {
                //查询数据库
                PdtDb.select(0) {
                    val fragments = arrayListOf<Fragment>()
                    val titles = arrayListOf<String>()
                    for (i in it) {
                        fragments.add(ContentFragment.newInstance(i.article_key))
                        titles.add(i.describe)
                    }

                    val adapter = TabAdapter(
                        supportFragmentManager,
                        FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,
                        fragments.toTypedArray(),
                        titles
                    )
                    viewpager.offscreenPageLimit = fragments.size
                    viewpager.adapter = adapter
                    tabLayout.setupWithViewPager(viewpager)
                }
            }
        }
        viewpager.setScrollable(true)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.more -> {//更多
                startActivity<ChannelActivity>()
            }
        }
    }
}