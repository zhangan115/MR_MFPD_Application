package com.mr.mf_pd.application.view.task

import android.os.Bundle
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.databinding.TaskDataBinding
import com.mr.mf_pd.application.view.base.AbsBaseActivity

class TaskActivity : AbsBaseActivity<TaskDataBinding>() {

    override fun initView(savedInstanceState: Bundle?) {

    }

    override fun initData(savedInstanceState: Bundle?) {

    }

    override fun getContentView(): Int {
        return R.layout.activity_task
    }

    override fun getToolBarTitle(): String {
        return "检测任务"
    }
}