package com.mr.mf_pd.application.view.check

import android.content.Intent
import android.os.Bundle
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.common.Constants
import com.mr.mf_pd.application.databinding.DeviceCheckDataBinding
import com.mr.mf_pd.application.manager.socket.CommandHelp
import com.mr.mf_pd.application.manager.socket.SocketManager
import com.mr.mf_pd.application.model.DeviceBean
import com.mr.mf_pd.application.view.base.AbsBaseActivity
import com.mr.mf_pd.application.view.check.ac.CheckACActivity
import com.mr.mf_pd.application.view.check.hf.CheckHFActivity
import com.mr.mf_pd.application.view.check.tev.CheckTEVActivity
import com.mr.mf_pd.application.view.check.uhf.CheckUHFActivity
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
        linkToDevice()
    }

    private fun linkToDevice() {
        SocketManager.getInstance().releaseRequest()
        SocketManager.getInstance().initLink()
        SocketManager.getInstance().addLinkStateListeners {
            if (it == Constants.LINK_SUCCESS) {
                SocketManager.getInstance().sendData(CommandHelp.getTimeCommand())
            } else {

            }
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

    override fun onDestroy() {
        super.onDestroy()
        SocketManager.getInstance().releaseRequest()
    }
}