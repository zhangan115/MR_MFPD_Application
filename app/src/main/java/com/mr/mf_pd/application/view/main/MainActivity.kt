package com.mr.mf_pd.application.view.main

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.anson.support.base.BaseActivity
import com.bumptech.glide.Glide
import com.mr.mf_pd.application.R
import com.qw.soul.permission.SoulPermission

class MainActivity : BaseActivity() {

    override fun getContentView(): Int {

        return R.layout.activity_main
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