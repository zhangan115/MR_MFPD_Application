package com.mr.mf_pd.application.view.splash

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.anson.support.base.BaseActivity
import com.anson.support.base.bindLifeCycle
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.utils.getViewModelFactory
import com.mr.mf_pd.application.view.main.MainActivity

class SplashActivity : BaseActivity() {

    lateinit var viewModel: SplashViewModel

    override fun initThem() {
        super.initThem()
        setStatusBarTransparent()
        setStatusBarDark(true)
    }

    override fun initView(savedInstanceState: Bundle?) {

    }

    override fun initData(savedInstanceState: Bundle?) {
        viewModel = getViewModelFactory().create(SplashViewModel::class.java)
    }

    override fun getToolBar(): Toolbar? {
        return null
    }

    override fun getToolBarTitleView(): TextView? {
        return null
    }

    override fun getContentView(): Int {
        return R.layout.activity_splash
    }

    override fun request() {
        viewModel.start().bindLifeCycle(this).subscribe { _ ->
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}