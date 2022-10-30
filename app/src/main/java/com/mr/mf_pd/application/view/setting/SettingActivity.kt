package com.mr.mf_pd.application.view.setting

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.adapter.ToastAdapter
import com.mr.mf_pd.application.app.MRApplication
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.databinding.SettingDataBinding
import com.mr.mf_pd.application.utils.FileUtils
import com.mr.mf_pd.application.utils.ZLog
import com.mr.mf_pd.application.utils.getViewModelFactory
import com.mr.mf_pd.application.view.base.AbsBaseActivity
import com.sito.tool.library.utils.SPHelper
import kotlinx.android.synthetic.main.activity_setting.*

class SettingActivity : AbsBaseActivity<SettingDataBinding>() {

    private val viewModel by viewModels<SettingViewModel> { getViewModelFactory() }
    private var cunt = 0
    private var cleanCount = 0
    override fun initView(savedInstanceState: Bundle?) {
        settingIPTv.setOnClickListener {
            cunt++
            if (cunt == 5) {
                saveLogLayout.visibility = View.VISIBLE
                cunt = 0
            }
        }
        setPortTv.setOnClickListener {
            cleanCount++
            if (cleanCount == 5) {
                val file = MRApplication.instance.fileCacheFile()
                file?.let {
                    val files = it.listFiles()?.filter { file ->
                        file.name.endsWith(".log")
                    }
                    ZLog.stopSaveLog()
                    FileUtils.deleteFiles(files, object : FileUtils.FileActionListener {
                        override fun onSuccess() {
                            ToastAdapter.bindToast(saveLogSwitch, "Log清除成功")
                        }

                        override fun onFail() {
                            ToastAdapter.bindToast(saveLogSwitch, "Log清除失败")
                        }
                    })
                }

                cleanCount = 0
            }
        }
        saveLogSwitch.setOnCheckedChangeListener { _, saveLog ->
            SPHelper.write(MRApplication.instance.applicationContext,
                ConstantStr.USER,
                ConstantStr.USER_CONFIG_LOG_OPEN,
                saveLog)
            if (!saveLog){
                ZLog.stopSaveLog()
            }
            ToastAdapter.bindToast(saveLogSwitch, "Log打开成功，请退出后重新进入程序")
        }
    }

    override fun initData(savedInstanceState: Bundle?) {
        dataBinding.vm = viewModel
        viewModel.start()
        val openLog = SPHelper.readBoolean(MRApplication.instance.applicationContext,
            ConstantStr.USER,
            ConstantStr.USER_CONFIG_LOG_OPEN,
            false)
        saveLogSwitch.isChecked = openLog
    }

    override fun getContentView(): Int {
        return R.layout.activity_setting
    }

    override fun getToolBarTitle(): String {
        return "常规设置"
    }


}