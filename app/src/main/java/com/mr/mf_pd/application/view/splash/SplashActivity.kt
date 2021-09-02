package com.mr.mf_pd.application.view.splash

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.databinding.SplashDataBinding
import com.mr.mf_pd.application.utils.getViewModelFactory
import com.mr.mf_pd.application.view.base.AbsBaseActivity
import com.mr.mf_pd.application.view.base.ext.bindLifeCycle
import com.mr.mf_pd.application.view.main.MainActivity

class SplashActivity : AbsBaseActivity<SplashDataBinding>() {

    private val viewModel by viewModels<SplashViewModel> { getViewModelFactory() }

    override fun initView(savedInstanceState: Bundle?) {
        setDarkStatusIcon(true)
    }

    override fun initData(savedInstanceState: Bundle?) {
        dataBinding.vm = viewModel
    }

    override fun getContentView(): Int {
        return R.layout.activity_splash
    }

    override fun requestData() {
        viewModel.start().bindLifeCycle(this).subscribe { _ ->
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}