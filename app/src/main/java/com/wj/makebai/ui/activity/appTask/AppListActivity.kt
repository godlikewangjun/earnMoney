package com.wj.makebai.ui.activity.appTask

import android.Manifest
import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import com.wj.eventbus.WjEventBus
import com.wj.makebai.R
import com.wj.makebai.statice.Codes
import com.wj.makebai.ui.activity.base.MakeActivity
import com.wj.makebai.ui.adapter.AppListAdapter
import com.wj.makebai.ui.weight.NoData
import com.wj.permission.PermissionUtils
import ebz.lsds.qamj.os.df.AppSummaryDataInterface
import ebz.lsds.qamj.os.df.AppSummaryObject
import ebz.lsds.qamj.os.df.AppSummaryObjectList
import ebz.lsds.qamj.os.df.DiyOfferWallManager
import kotlinx.android.synthetic.main.activity_applist_layout.*

/**
 * 积分墙的任务
 * @author dchain
 * @version 1.0
 * @date 2019/10/22
 */
class AppListActivity : MakeActivity(){

    override fun bindLayout(): Int {
        return R.layout.activity_applist_layout
    }

    override fun initData() {
        setState(NoData.DataState.LOADING,false)
        PermissionUtils.permission(
            activity!!,
            Manifest.permission.READ_PHONE_STATE
        )
            .rationale(object : PermissionUtils.OnRationaleListener {
                override fun rationale(shouldRequest: PermissionUtils.OnRationaleListener.ShouldRequest) {
                    shouldRequest.again(true)
                }
            })
            .callback(object : PermissionUtils.FullCallback {
                override fun onDenied(
                    permissionsDeniedForever: ArrayList<String>?,
                    permissionsDenied: ArrayList<String>
                ) {
                    finish()
                }

                override fun onGranted(permissionsGranted: ArrayList<String>) {
                    loadList()
                }
            }).request()
    }

    /**
     * 加载数据
     */
    private fun loadList() {
        DiyOfferWallManager.getInstance(activity)
            .loadOfferWallAdList(DiyOfferWallManager.ym_param_REQUEST_SPECIAL_SORT, 1, 100, object :
                AppSummaryDataInterface {
                override fun onLoadAppSumDataSuccess(p0: Context?, adList: AppSummaryObjectList?) {
                    if (adList != null && adList.size() > 0) {
                        val list = ArrayList<AppSummaryObject>()
                        for (index in 0 until adList.size()) {
                            list.add(adList[index])
                        }
                        runOnUiThread {
                            app_list.adapter = AppListAdapter(list)
                            app_list.layoutManager = LinearLayoutManager(activity)
                            setState(NoData.DataState.GONE, false)
                        }
                    } else {
                        runOnUiThread {
                            setState(NoData.DataState.NULL, false)
                        }
                    }
                }

                override fun onLoadAppSumDataFailed() {
                    runOnUiThread {
                        setState(NoData.DataState.NULL, false)
                    }
                }

                override fun onLoadAppSumDataFailedWithErrorCode(p0: Int) {
                    runOnUiThread {
                        setState(NoData.DataState.NULL, false)
                    }
                }

            })
    }

    override fun onDestroy() {
        super.onDestroy()
        WjEventBus.getInit().remove(Codes.TASKFINSH)
        DiyOfferWallManager.getInstance(this).onAppExit()
    }
}