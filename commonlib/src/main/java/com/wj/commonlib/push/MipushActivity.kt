package com.wj.commonlib.push

import android.content.Intent
import android.os.Bundle
import com.abase.util.AbLogUtil
import com.umeng.message.UmengNotifyClickActivity
import org.android.agoo.common.AgooConstants
/**
 * 小米弹窗功能
 * @author admin
 * @version 1.0
 * @date 2020/11/23
 */
class MipushActivity : UmengNotifyClickActivity(){
    override fun onCreate(p0: Bundle?) {
        super.onCreate(p0)
    }

    override fun onMessage(p0: Intent?) {
        super.onMessage(p0)
        val body = intent.getStringExtra(AgooConstants.MESSAGE_BODY)
        startActivity(Intent().apply {
            setClassName(
                packageName,
                "$packageName.ui.activity.PermissionCheckActivity"
            )
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
        AbLogUtil.d(MipushActivity::class.java,body)
        finish()
    }
}