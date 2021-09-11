package com.mr.mf_pd.application.view.main

import android.os.Bundle
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.databinding.DeviceCheckDataBinding
import com.mr.mf_pd.application.view.base.AbsBaseActivity
import kotlinx.android.synthetic.main.activity_device_check.*

class DeviceCheckActivity : AbsBaseActivity<DeviceCheckDataBinding>() {

    override fun initView(savedInstanceState: Bundle?) {
        uhfDataLayout.setOnClickListener {

        }
        acTaskLayout.setOnClickListener {

        }
        tevDataLayout.setOnClickListener {

        }
        hfTaskLayout.setOnClickListener {

        }
        checkDataLayout.setOnClickListener {

        }
        selfCheckingLayout.setOnClickListener {

        }
    }

    override fun initData(savedInstanceState: Bundle?) {

    }

    override fun getContentView(): Int {
        return R.layout.activity_device_check
    }
}