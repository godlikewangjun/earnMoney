package com.wj.makebai.ui.adapter

import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

/**
 * table 切换
 * @author wangjun
 * @version 1.0
 * @date 2017/12/30
 */

class TabAdapter(fm: FragmentManager, behavior: Int) : FragmentPagerAdapter(fm, behavior) {
    var fragments: Array<Fragment>? = null
    var titles: List<String>? = null

    constructor(fm: FragmentManager, behavior: Int, fragments: Array<Fragment>?) : this(
        fm,
        behavior
    ) {
        this.fragments = fragments
    }

    constructor(
        fm: FragmentManager,
        behavior: Int,
        fragments: Array<Fragment>?,
        titles: List<String>?
    ) : this(fm, behavior) {
        this.fragments = fragments
        this.titles = titles
    }

    override fun getItem(position: Int): Fragment {
        return fragments!![position]
    }

    override fun getCount(): Int {
        return fragments!!.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return if (titles != null) titles!![position] else position.toString()
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
//        super.destroyItem(container, position, `object`)
    }
}
