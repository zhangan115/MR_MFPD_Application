package com.mr.mf_pd.application.view.setting

import android.os.Bundle
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.databinding.SettingDataBinding
import com.mr.mf_pd.application.view.base.AbsBaseActivity

class SettingActivity : AbsBaseActivity<SettingDataBinding>() {

    override fun initView(savedInstanceState: Bundle?) {

    }

    override fun initData(savedInstanceState: Bundle?) {

    }

    override fun getContentView(): Int {
        return R.layout.activity_setting
    }

    override fun getToolBarTitle(): String {
        return "常规设置"
    }
}