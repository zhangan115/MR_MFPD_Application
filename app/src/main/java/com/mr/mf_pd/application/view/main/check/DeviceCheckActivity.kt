package com.mr.mf_pd.application.view.main.check

import android.content.Intent
import android.os.Bundle
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.databinding.DeviceCheckDataBinding
import com.mr.mf_pd.application.manager.ReadListener
import com.mr.mf_pd.application.manager.SocketManager
import com.mr.mf_pd.application.model.DeviceBean
import com.mr.mf_pd.application.view.ac.CheckACActivity
import com.mr.mf_pd.application.view.base.AbsBaseActivity
import com.mr.mf_pd.application.view.hf.CheckHFActivity
import com.mr.mf_pd.application.view.tev.CheckTEVActivity
import com.mr.mf_pd.application.view.uhf.CheckUHFActivity
import kotlinx.android.synthetic.main.activity_device_check.*
import java.util.*

class DeviceCheckActivity : AbsBaseActivity<DeviceCheckDataBinding>() {

    private var mDeviceBean: DeviceBean? = null

    override fun initView(savedInstanceState: Bundle?) {
        uhfDataLayout.setOnClickListener {
            val intent = Intent(this, CheckUHFActivity::class.java)
            intent.putExtra(ConstantStr.KEY_BUNDLE_OBJECT, mDeviceBean)
            startActivity(intent)
        }
        acTaskLayout.setOnClickListener {
            val intent = Intent(this, CheckACActivity::class.java)
            intent.putExtra(ConstantStr.KEY_BUNDLE_OBJECT, mDeviceBean)
            startActivity(intent)
        }
        tevDataLayout.setOnClickListener {
            val intent = Intent(this, CheckTEVActivity::class.java)
            intent.putExtra(ConstantStr.KEY_BUNDLE_OBJECT, mDeviceBean)
            startActivity(intent)
        }
        hfTaskLayout.setOnClickListener {
            val intent = Intent(this, CheckHFActivity::class.java)
            intent.putExtra(ConstantStr.KEY_BUNDLE_OBJECT, mDeviceBean)
            startActivity(intent)
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

    private val readListener = object : ReadListener(1) {

        override fun onRead(bytes: ByteArray?) {

        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}