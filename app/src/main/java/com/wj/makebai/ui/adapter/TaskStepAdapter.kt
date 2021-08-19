package com.wj.makebai.ui.adapter

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.abase.view.weight.RecyclerSpace
import com.wj.commonlib.data.mode.TaskImageMode
import com.wj.commonlib.data.mode.TaskStep
import com.wj.commonlib.ui.weight.CustomGridManager
import com.wj.commonlib.utils.CommTools
import com.wj.ktutils.startActivity
import com.wj.makebai.R
import com.wj.makebai.ui.activity.comm.WebActivity
import com.wj.makebai.ui.adapter.task.TaskImgAdapter
import com.wj.ui.base.BaseAdapter
import com.wj.ui.base.viewhoder.CustomVhoder
import com.zzhoujay.richtext.RichText
import kotlinx.android.synthetic.main.step_copy_item_layout.view.*
import kotlinx.android.synthetic.main.step_img_item_layout.view.*
import kotlinx.android.synthetic.main.step_info_item_layout.view.*
import kotlinx.android.synthetic.main.step_link_item_layout.view.*


/**
 * 任务步骤
 * @author Administrator
 * @version 1.0
 * @date 2020/8/13
 */
class TaskStepAdapter() : BaseAdapter() {
    enum class StepEnum {
        IMG, INFO, LINK, COPY
    }

    var taskSteps: List<TaskStep>? = null
    var submitInfo = HashMap<Int, Any>()//需要提交的信息
    var isGetTask = false//是否已经领取任务了

    constructor(task_steps: List<TaskStep>?) : this() {
        this.taskSteps = task_steps
    }


    override fun getItemCount(): Int {
        return taskSteps!!.size
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val mode = taskSteps!![position]
        holder.itemView.findViewById<TextView>(R.id.num).text = (position + 1).toString()
        holder.itemView.findViewById<TextView>(R.id.task_desc).text = Html.fromHtml(mode.stepDesc)

        when (mode.stepType) {
            StepEnum.IMG.ordinal -> {
                val list = ArrayList<TaskImageMode>()
                for (index in mode.stepData as ArrayList<String>) {
                    list.add(TaskImageMode(0, index))
                }
                if (mode.isCommit) {
                    for (index in mode.commitInfo as List<String>) {
                        list.add(TaskImageMode(2, index))
                    }
                }
                val adapter = TaskImgAdapter(list)
                adapter.isGetTask = isGetTask
                adapter.type = mode.stepCommit
                adapter.imageChange = fun(imgs: ArrayList<String>) {
                    submitInfo[mode.taskStepId] = imgs
                }
                holder.itemView.recyclerView.adapter = adapter
                holder.itemView.recyclerView.layoutManager = CustomGridManager(context, 3)
                if (holder.itemView.recyclerView.itemDecorationCount < 1) holder.itemView.recyclerView.addItemDecoration(
                    RecyclerSpace(10)
                )
            }
            StepEnum.INFO.ordinal -> {
                if (mode.stepCommit == 0) {
                    holder.itemView.task_info.isEnabled = false
                    RichText.from(mode.stepData as String).into(holder.itemView.task_info)
                    holder.itemView.task_info.setBackgroundDrawable(null)
                } else {//需要提交
                    holder.itemView.task_info.setBackgroundDrawable(
                        context!!.resources.getDrawable(
                            R.drawable.shape_gray_line1
                        )
                    )
                    if (isGetTask) holder.itemView.task_info.isEnabled = true
                    holder.itemView.task_info.hint = mode.stepData as String
                    if (mode.isCommit) {
                        holder.itemView.task_info.isEnabled = false
                        holder.itemView.task_info.setText(mode.commitInfo as String)
                    }
                    holder.itemView.task_info.addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(
                            s: CharSequence?,
                            start: Int,
                            count: Int,
                            after: Int
                        ) {
                        }

                        override fun onTextChanged(
                            s: CharSequence?,
                            start: Int,
                            before: Int,
                            count: Int
                        ) {
                        }

                        override fun afterTextChanged(s: Editable?) {
                            if (s.toString().trim().isNotEmpty()) {
                                submitInfo[mode.taskStepId] = s.toString()
                            }
                        }

                    })
                }
            }
            StepEnum.COPY.ordinal -> {
                holder.itemView.task_copy_info.text = mode.stepData as String
                holder.itemView.setOnClickListener {
                    CommTools.copy(
                        context!!,
                        mode.stepData as String
                    )
                }
            }
            StepEnum.LINK.ordinal -> {
                holder.itemView.task_link_url.text = mode.stepData as String
                holder.itemView.task_open.setOnClickListener {
                    context!!.startActivity<WebActivity>(
                        "title" to mode.stepDesc,
                        "url" to mode.stepData as String
                    )
//                    val uri: Uri = Uri.parse(mode.stepData as String)
//                    val intent = Intent()
//                    intent.action = "android.intent.action.VIEW"
//                    intent.data = uri
//                    context!!.startActivity(intent)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        super.onCreateViewHolder(parent, viewType)

        return CustomVhoder(
            when (viewType) {
                StepEnum.IMG.ordinal -> {
                    inflater!!.inflate(R.layout.step_img_item_layout, parent, false)
                }
                StepEnum.INFO.ordinal -> {
                    inflater!!.inflate(R.layout.step_info_item_layout, parent, false)
                }
                StepEnum.COPY.ordinal -> {
                    inflater!!.inflate(R.layout.step_copy_item_layout, parent, false)
                }
                StepEnum.LINK.ordinal -> {
                    inflater!!.inflate(R.layout.step_link_item_layout, parent, false)
                }
                else -> inflater!!.inflate(R.layout.step_img_item_layout, parent, false)
            }
        )
    }

    override fun getItemViewType(position: Int): Int {
        return taskSteps!![position].stepType
    }
}