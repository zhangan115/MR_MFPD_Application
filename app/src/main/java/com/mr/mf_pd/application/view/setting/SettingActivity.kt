package com.mr.mf_pd.application.view.setting

import android.os.Bundle
import androidx.activity.viewModels
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.databinding.SettingDataBinding
import com.mr.mf_pd.application.utils.getViewModelFactory
import com.mr.mf_pd.application.view.base.AbsBaseActivity

class SettingActivity : AbsBaseActivity<SettingDataBinding>() {

    private val viewModel by viewModels<SettingViewModel> { getViewModelFactory() }

    override fun initView(savedInstanceState: Bundle?) {

    }

    override fun initData(savedInstanceState: Bundle?) {
        dataBinding.vm = viewModel
        viewModel.start()
    }

    override fun getContentView(): Int {
        return R.layout.activity_setting
    }

    override fun getToolBarTitle(): String {
        return "常规设置"
    }
}