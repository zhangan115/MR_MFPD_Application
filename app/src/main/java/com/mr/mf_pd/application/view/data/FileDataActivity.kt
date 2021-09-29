package com.mr.mf_pd.application.view.data

import android.os.Bundle
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.databinding.FileDataDataBinding
import com.mr.mf_pd.application.utils.DataUtil
import com.mr.mf_pd.application.view.base.AbsBaseActivity
import java.io.File

class FileDataActivity : AbsBaseActivity<FileDataDataBinding>() {

    lateinit var currentFile: File
    private var toolbarTitleStr = ""

    override fun initView(savedInstanceState: Bundle?) {

    }

    override fun initData(savedInstanceState: Bundle?) {
        intent.getStringExtra(ConstantStr.KEY_BUNDLE_STR)?.let {
            currentFile = File(it)
            //todo 暂时的标题
            toolbarTitleStr = DataUtil.timeFormat(System.currentTimeMillis(), "yyyy MM dd HH:mm")
        }
    }

    override fun getContentView(): Int {
        return R.layout.activity_file_data
    }

    override fun getToolBarTitle(): String {
        return toolbarTitleStr
    }

}