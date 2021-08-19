package com.wj.makebai.utils

import com.abase.okhttp.OhHttpClient
import com.wj.commonlib.data.mode.VideoMode
import com.wj.ktutils.WjSP
import com.wj.ktutils.isNull
import com.wj.makebai.data.emu.ArtEmu
import com.wj.makebai.data.mode.ArtTypeMode
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import java.io.IOException

/**
 * 短视频请求处理
 * @author admin
 * @version 1.0
 * @date 2020/1/16
 */
class ShortVideoRequest {
    /**
     * 获取首页列表
     */
    fun getList(urlHtml: String, getHeader: Boolean, success: (ArrayList<ArtTypeMode>) -> Unit) {
        val builder: Request.Builder =
            Request.Builder().url(urlHtml).tag(urlHtml) // 设置tag
        builder.get()
        val callback = object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                val string = WjSP.getInstance()
                    .getValues(urlHtml, "")
                if (!string.isNull()) {
                    getData(urlHtml, string, getHeader, success)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val string = response.body!!.string()
                if(string.isNull()){
                    return success.invoke(ArrayList())
                }
                //保存
                WjSP.getInstance()
                    .setValues(urlHtml, string)
                getData(urlHtml, string, getHeader, success)
            }
        }
        OhHttpClient.getInit().client.newCall(builder.build()).enqueue(callback)
    }

    /**
     * 解析
     */
    private fun getData(
        urlHtml: String,
        string: String,
        getHeader: Boolean,
        success: (ArrayList<ArtTypeMode>) -> Unit
    ) {
        val videoModeList = ArrayList<ArtTypeMode>()
        try {
            val doc = Jsoup.parse(string.byteInputStream(), "utf-8", urlHtml)
            //获取首页的视频
            val homePushTypes = doc.select("body > div.container > div.left")[0]
            val homePushTypeChild = homePushTypes.getElementsByClass("vbox clearfix")
            for (index in homePushTypeChild) {
                if (getHeader) {
                    val header = index.getElementsByClass("hd-title")[0]
                    var title = header.text()
                    if (header.children().size > 1) {
                        title = header.children()[1].text()
                    }
                    videoModeList.add(ArtTypeMode(title, ArtEmu.HEADER))
                }

                val box = index.getElementsByClass("box")[0]
                var picList: Elements?
                picList = if (box.getElementsByClass("piclist2").size > 0) {
                    box.getElementsByClass("piclist2")[0].child(0).children()
                } else {
                    box.getElementsByClass("piclist")[0].child(0).children()
                }
                for (li in picList) {
                    val child = li.child(0)
                    var hd = ""
                    val hdElement = child.getElementsByClass("hd")
                    if (hdElement.size > 0) hd = hdElement[0].text()
                    val mode = VideoMode(
                        child.child(0).absUrl("src"),
                        child.absUrl("href"),
                        child.getElementsByClass("time")[0].text(),
                        hd,
                        child.getElementsByClass("title")[0].text()
                    )

                    val videoMode = ArtTypeMode(mode, ArtEmu.ART)
                    if (!videoModeList.hasItem(mode)) videoModeList.add(videoMode)
                }

            }
            success.invoke(videoModeList)
        } catch (e: Exception) {
            e.printStackTrace()
            success.invoke(videoModeList)
        }
    }

    /**
     * 获取首页列表
     */
    fun getOtherList(urlHtml: String, success: (ArrayList<ArtTypeMode>) -> Unit) {
        val builder: Request.Builder =
            Request.Builder().url(urlHtml).tag(urlHtml) // 设置tag
        builder.get()
        val callback = object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                val string = WjSP.getInstance()
                    .getValues(urlHtml, "")
                if (!string.isNull()) {
                    getHomeData(urlHtml, string, success)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val string = response.body!!.string()
                //保存
                WjSP.getInstance()
                    .setValues(urlHtml, string)
                getHomeData(urlHtml, string, success)
            }
        }
        OhHttpClient.getInit().client.newCall(builder.build()).enqueue(callback)
    }

    private fun getHomeData(
        urlHtml: String,
        string: String,
        success: (ArrayList<ArtTypeMode>) -> Unit
    ) {
        val doc = Jsoup.parse(string.byteInputStream(), "utf-8", urlHtml)
        val videoModeList = ArrayList<ArtTypeMode>()
        //获取首页的视频
        val homePushTypes = doc.select("body > div.container > div.left")[0]
        val homePushTypeChild = homePushTypes.getElementsByClass("vbox clearfix")
        for (index in homePushTypeChild) {
            val box = index.getElementsByClass("box")[0]
            var picList: Elements?
            picList = if (box.getElementsByClass("piclist2").size > 0) {
                box.getElementsByClass("piclist2")[0].child(0).children()
            } else {
                box.getElementsByClass("piclist")[0].child(0).children()
            }
            for (li in picList) {
                val child = li.child(0)
                var hd = ""
                val hdElement = child.getElementsByClass("hd")
                if (hdElement.size > 0) hd = hdElement[0].text()
                val mode = VideoMode(
                    child.child(0).absUrl("src"),
                    child.absUrl("href"),
                    child.getElementsByClass("time")[0].text(),
                    hd,
                    child.getElementsByClass("title")[0].text()
                )

                val videoMode = ArtTypeMode(mode, ArtEmu.ART)
                if (!videoModeList.hasItem(mode)) videoModeList.add(videoMode)
            }

        }
        success.invoke(videoModeList)
    }

    /**
     * 判断是否已经添加
     */
    private fun ArrayList<ArtTypeMode>.hasItem(videoMode: VideoMode): Boolean {
        for (index in this) {
            if (index.type == ArtEmu.ART && (index.mode as VideoMode).url == videoMode.url) return true
        }
        return false
    }
}