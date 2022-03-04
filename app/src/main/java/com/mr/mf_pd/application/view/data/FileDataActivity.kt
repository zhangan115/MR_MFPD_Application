package com.mr.mf_pd.application.view.data

import android.os.Bundle
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.databinding.FileDataDataBinding
import com.mr.mf_pd.application.utils.FileTypeUtils
import com.mr.mf_pd.application.utils.FileUtils
import com.mr.mf_pd.application.view.base.AbsBaseActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

class FileDataActivity : AbsBaseActivity<FileDataDataBinding>() {

    lateinit var currentFile: File
    val checkType: CheckType? = null
    private var toolbarTitleStr = ""

    override fun initView(savedInstanceState: Bundle?) {

    }

    override fun initData(savedInstanceState: Bundle?) {
        intent.getStringExtra(ConstantStr.KEY_BUNDLE_STR)?.let {
            currentFile = File(it)
            GlobalScope.runCatching {
               val checkDataFileModel =  FileUtils.isCheckDataFile(currentFile)
                GlobalScope.launch(Dispatchers.Main) {
                    val fileTypeNameStr = FileTypeUtils.getCheckTypeStr(checkDataFileModel!!.fileType!!)
                    val titleName = currentFile.name.substring(fileTypeNameStr.length,currentFile.name.length)
                    setTitleValue(titleName + findString(checkDataFileModel.fileType!!.description))
                }
            }
        }
    }

    override fun getContentView(): Int {
        return R.layout.activity_file_data
    }

    override fun getToolBarTitle(): String {
        return toolbarTitleStr
    }

}