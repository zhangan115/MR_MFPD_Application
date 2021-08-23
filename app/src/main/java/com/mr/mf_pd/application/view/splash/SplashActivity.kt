package com.mr.mf_pd.application.view.splash

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.anson.support.base.BaseActivity
import com.mr.mf_pd.application.R

class SplashActivity : BaseActivity() {
    override fun getContentView(): Int {
        return R.layout.activity_splash
    }

    override fun getToolBar(): Toolbar? {
        return null
    }

    override fun getToolBarTitleView(): TextView? {
        return null
    }

    override fun initData(savedInstanceState: Bundle?) {

    }

    override fun initView(savedInstanceState: Bundle?) {

    }
}