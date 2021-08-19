package com.wj.makebai.ui.activity.zip

import android.graphics.BitmapFactory
import androidx.recyclerview.widget.GridLayoutManager
import com.abase.view.weight.RecyclerSpace
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.listener.OnResultCallbackListener
import com.wj.commonlib.utils.CommTools
import com.wj.commonlib.utils.LoadDialog
import com.wj.ktutils.isNull
import com.wj.ktutils.showTip
import com.wj.makebai.R
import com.wj.makebai.ui.activity.base.MakeActivity
import com.wj.makebai.ui.adapter.GridImageAdapter
import com.wj.commonlib.utils.GlideEngine
import kotlinx.android.synthetic.main.activity_images_zip.*
import kotlinx.android.synthetic.main.gv_filter_image.view.*
import java.io.File


/**
 * 图片压缩
 * @author wangjun
 * @version 1.0
 * @date 2020/2/5
 */
class ImageZipActivity : MakeActivity() {
    private var mAdapter: GridImageAdapter? = null
    private var saveFiles=ArrayList<File>()
    override fun bindLayout(): Int {
        return R.layout.activity_images_zip
    }

    override fun initData() {
        title_content.text=getString(R.string.zipImg)

        val manager = GridLayoutManager(
            this,
            3, GridLayoutManager.VERTICAL, false
        )
        recycler.layoutManager = manager

        recycler.addItemDecoration(RecyclerSpace(10))
        mAdapter = GridImageAdapter(onAddPicClickListener).setSelectMax(9)
        recycler.adapter=mAdapter

        zip.setOnClickListener {
            if(!zip.isSelected){
                if(mAdapter!!.list.isEmpty()){
                    return@setOnClickListener showTip("没有可压缩的图片")
                }
                deleteZipFile()
                saveFiles.clear()
                LoadDialog.show(activity)
                CommTools.zipImages(activity,mAdapter!!.list.toFiles(),object: CommTools.ZipListener{
                    override fun zipImagesSuccess(zipImg: ArrayList<File>) {
                        saveFiles.addAll(zipImg)
                        LoadDialog.cancle()
                        zip.text=getString(R.string.saveToPhotos)
                        zip.isSelected=true
                        for (index in 0 until recycler.childCount-1){
                            recycler.getChildAt(index).ad_size.textSize=10f
                            recycler.getChildAt(index).ad_size.append(" 压缩后：${CommTools.kbM(saveFiles[index].length())}")
                        }
                    }
                })
            }else{
                LoadDialog.show(activity)
                for (index in saveFiles){
                    CommTools.saveBmp2Gallery(activity,BitmapFactory.decodeFile(index.path),index.name)
                }
                deleteZipFile()
                LoadDialog.cancle()
                showTip("保存成功")
            }
        }
    }

    /**
     * 删除压缩后的图片
     */
    private fun deleteZipFile(){
        //删除压缩后的数据
        for (index in saveFiles){
            index.delete()
        }
    }
    /**
     * 转化
     */
    private fun ArrayList<LocalMedia>.toFiles():ArrayList<File>{
        val files=ArrayList<File>()
        for (media in this){
            val path: String =  if(!media.androidQToPath.isNull()) media.androidQToPath else media.path
            files.add(File(path))
        }
        return files
    }

    /**
     * 图片选择
     */
    private val onAddPicClickListener = object : GridImageAdapter.onAddPicClickListener {
        override fun onAddPicClick() {
            PictureSelector.create(activity)
                .openGallery(PictureMimeType.ofImage())
                .compress(false)
                .maxSelectNum(9)
                .loadImageEngine(GlideEngine.createGlideEngine()) // 请参考Demo GlideEngine.java
                .forResult(object : OnResultCallbackListener {
                    override fun onResult(result: List<LocalMedia>) {
                        if(result.isNotEmpty()){
                            zip.text=getString(R.string.start_zip)
                            zip.isSelected=false
                        }
                        mAdapter!!.list.addAll(result)
                        mAdapter!!.notifyDataSetChanged()
                    }

                    fun onCancel() {
                    }
                })
        }
    }

    override fun onDestroy() {
        deleteZipFile()
        super.onDestroy()
    }
}