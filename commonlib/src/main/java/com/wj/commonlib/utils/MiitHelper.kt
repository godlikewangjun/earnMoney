package com.wj.commonlib.utils

import android.content.Context
import android.util.Log
import com.bun.miitmdid.core.ErrorCode
import com.bun.miitmdid.core.IIdentifierListener
import com.bun.miitmdid.core.MdidSdk
import com.bun.miitmdid.core.MdidSdkHelper
import com.bun.miitmdid.supplier.IdSupplier

/**
 * Created by zheng on 2019/8/22.
 */
class MiitHelper(private val _listener: AppIdsUpdater?) : IIdentifierListener {
    fun getDeviceIds(cxt: Context) {
        val timeb = System.currentTimeMillis()
        val nres = CallFromReflect(cxt)
        //        int nres=DirectCall(cxt);
        val timee = System.currentTimeMillis()
        val offset = timee - timeb
        when (nres) {
            ErrorCode.INIT_ERROR_DEVICE_NOSUPPORT -> { //不支持的设备
            }
            ErrorCode.INIT_ERROR_LOAD_CONFIGFILE -> { //加载配置文件出错
            }
            ErrorCode.INIT_ERROR_MANUFACTURER_NOSUPPORT -> { //不支持的设备厂商
            }
            ErrorCode.INIT_ERROR_RESULT_DELAY -> { //获取接口是异步的，结果会在回调中返回，回调执行的回调可能在工作线程
            }
            ErrorCode.INIT_HELPER_CALL_ERROR -> { //反射调用出错
            }
        }
        Log.d(javaClass.simpleName, "return value: $nres")
    }

    /*
    * 通过反射调用，解决android 9以后的类加载升级，导至找不到so中的方法
    *
    * */
    private fun CallFromReflect(cxt: Context): Int {
        return MdidSdkHelper.InitSdk(cxt, true, this)
    }

    /*
    * 直接java调用，如果这样调用，在android 9以前没有题，在android 9以后会抛找不到so方法的异常
    * 解决办法是和JLibrary.InitEntry(cxt)，分开调用，比如在A类中调用JLibrary.InitEntry(cxt)，在B类中调用MdidSdk的方法
    * A和B不能存在直接和间接依赖关系，否则也会报错
    *
    * */
    private fun DirectCall(cxt: Context): Int {
        val sdk = MdidSdk()
        return sdk.InitSdk(cxt, this)
    }

    override fun OnSupport(isSupport: Boolean, _supplier: IdSupplier) {
        if (_supplier == null) {
            return
        }
        val oaid = _supplier.oaid
        val vaid = _supplier.vaid
        val aaid = _supplier.aaid
        val builder = StringBuilder()
        builder.append("support: ").append(if (isSupport) "true" else "false").append("\n")
        builder.append("OAID: ").append(oaid).append("\n")
        builder.append("VAID: ").append(vaid).append("\n")
        builder.append("AAID: ").append(aaid).append("\n")
        val idstext = builder.toString()
        _supplier.shutDown()
        _listener?.OnIdsAvalid(oaid)
    }

    interface AppIdsUpdater {
        fun OnIdsAvalid(ids: String?)
    }

}