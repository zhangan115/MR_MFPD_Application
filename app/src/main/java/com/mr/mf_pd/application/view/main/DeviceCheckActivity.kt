package com.mr.mf_pd.application.view.main

import android.content.Intent
import android.os.Bundle
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.databinding.DeviceCheckDataBinding
import com.mr.mf_pd.application.model.DeviceBean
import com.mr.mf_pd.application.view.base.AbsBaseActivity
import com.mr.mf_pd.application.view.uhf.CheckUHFActivity
import kotlinx.android.synthetic.main.activity_device_check.*

class DeviceCheckActivity : AbsBaseActivity<DeviceCheckDataBinding>() {

    private var mDeviceBean: DeviceBean? = null

    override fun initView(savedInstanceState: Bundle?) {
        uhfDataLayout.setOnClickListener {
            val intent = Intent(this, CheckUHFActivity::class.java)
            intent.putExtra(ConstantStr.KEY_BUNDLE_OBJECT, mDeviceBean)
            startActivity(intent)
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
        mDeviceBean = intent.getParcelableExtra(ConstantStr.KEY_BUNDLE_OBJECT)
    }

    override fun getToolBarTitle(): String? {
        return this.mDeviceBean?.deviceName
    }

    override fun getContentView(): Int {
        return R.layout.activity_device_check
    }
}