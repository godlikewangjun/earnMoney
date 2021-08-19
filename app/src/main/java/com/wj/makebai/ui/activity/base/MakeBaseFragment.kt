package com.wj.makebai.ui.activity.base

import com.umeng.analytics.MobclickAgent
import com.wj.ui.base.BaseFragment

/**
 * 基础fragment
 * @author dchain
 * @version 1.0
 * @date 2019/5/20
 */
abstract class MakeBaseFragment :BaseFragment(){
    var pageName: String = "fragment"
    var isInitFinish=false
    override fun init() {
        isInitFinish=true
    }

    override fun setContentView(): Int {
        pageName=setPageName()
        return 0
    }

    /**
     * 设置fragment名字方便统计
     */
    abstract fun setPageName():String
    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if(isVisibleToUser) MobclickAgent.onPageStart(pageName)
        else MobclickAgent.onPageEnd(pageName)
    }

    override fun onStart() {
        super.onStart()
        MobclickAgent.onPageStart(pageName)
    }

    override fun onDestroy() {
        super.onDestroy()
        MobclickAgent.onPageEnd(pageName)
    }
}