package com.mr.mf_pd.application.view.main

import android.os.Bundle
import androidx.activity.viewModels
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.databinding.MainDataBinding
import com.mr.mf_pd.application.utils.getViewModelFactory
import com.mr.mf_pd.application.view.base.AbsBaseActivity

class MainActivity : AbsBaseActivity<MainDataBinding>() {

    private val viewModel by viewModels<MainViewModel> { getViewModelFactory() }

    override fun initView(savedInstanceState: Bundle?) {

    }

    override fun initData(savedInstanceState: Bundle?) {
        dataBinding.vm = viewModel
    }

    override fun getContentView(): Int {
        return R.layout.activity_main
    }


}