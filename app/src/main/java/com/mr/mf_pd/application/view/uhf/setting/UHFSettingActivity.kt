package com.mr.mf_pd.application.view.uhf.setting

import android.os.Bundle
import androidx.activity.viewModels
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.databinding.UHFSettingDataBinding
import com.mr.mf_pd.application.utils.getViewModelFactory
import com.mr.mf_pd.application.view.base.AbsBaseActivity

class UHFSettingActivity : AbsBaseActivity<UHFSettingDataBinding>() {

    private val viewModel by viewModels<UHFSettingViewModel> { getViewModelFactory() }

    override fun initView(savedInstanceState: Bundle?) {

    }

    override fun initData(savedInstanceState: Bundle?) {
        dataBinding.vm = viewModel
        viewModel.start()
    }

    override fun getToolBarTitle(): String {
        return "特高频设置"
    }

    override fun getContentView(): Int {
        return R.layout.activity_uhf_setting
    }
}