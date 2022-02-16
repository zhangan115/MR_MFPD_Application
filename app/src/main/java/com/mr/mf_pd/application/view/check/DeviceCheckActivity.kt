package com.mr.mf_pd.application.view.check

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.adapter.ToastAdapter
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.common.Constants
import com.mr.mf_pd.application.databinding.DeviceCheckDataBinding
import com.mr.mf_pd.application.manager.socket.CommandHelp
import com.mr.mf_pd.application.manager.socket.CommandType
import com.mr.mf_pd.application.manager.socket.SocketManager
import com.mr.mf_pd.application.model.DeviceBean
import com.mr.mf_pd.application.view.base.AbsBaseActivity
import com.mr.mf_pd.application.view.check.ac.CheckACActivity
import com.mr.mf_pd.application.view.check.hf.CheckHFActivity
import com.mr.mf_pd.application.view.check.tev.CheckTEVActivity
import com.mr.mf_pd.application.view.check.uhf.CheckUHFActivity
import kotlinx.android.synthetic.main.activity_device_check.*
import java.util.*

class DeviceCheckActivity : AbsBaseActivity<DeviceCheckDataBinding>() {

    private var mDeviceBean: DeviceBean? = null

    override fun initView(savedInstanceState: Bundle?) {
        uhfDataLayout.setOnClickListener {
            val intent = Intent(this, CheckUHFActivity::class.java)
            intent.putExtra(ConstantStr.KEY_BUNDLE_OBJECT, CheckType.UHF)
            intent.putExtra(ConstantStr.KEY_BUNDLE_OBJECT_1, mDeviceBean)
            startActivity(intent)
        }
        aeTaskLayout.setOnClickListener {
            val intent = Intent(this, CheckACActivity::class.java)
            intent.putExtra(ConstantStr.KEY_BUNDLE_OBJECT, CheckType.AE)
            intent.putExtra(ConstantStr.KEY_BUNDLE_OBJECT_1, mDeviceBean)
            startActivity(intent)
        }
        tevDataLayout.setOnClickListener {
            val intent = Intent(this, CheckTEVActivity::class.java)
            intent.putExtra(ConstantStr.KEY_BUNDLE_OBJECT, CheckType.TEV)
            intent.putExtra(ConstantStr.KEY_BUNDLE_OBJECT_1, mDeviceBean)
            startActivity(intent)
        }
        hfTaskLayout.setOnClickListener {
            val intent = Intent(this, CheckHFActivity::class.java)
            intent.putExtra(ConstantStr.KEY_BUNDLE_OBJECT, CheckType.HF)
            intent.putExtra(ConstantStr.KEY_BUNDLE_OBJECT_1, mDeviceBean)
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
                val timeBytes = CommandHelp.getTimeCommand()
                SocketManager.getInstance().sendData(timeBytes, CommandType.SendTime) { bytes ->
                    if (Arrays.equals(timeBytes, bytes)) {
                        ToastAdapter.bindToast(uhfDataLayout, "对时成功")
                        //解决不断上传的问题，对时成功后先关闭采集通道
                        val close = CommandHelp.closePassageway()
                        SocketManager.getInstance()
                            .sendData(close, CommandType.SwitchPassageway, null)
                    } else {
                        ToastAdapter.bindToast(uhfDataLayout, "对时失败")
                    }
                }
            } else {
                runOnUiThread {
                    ToastAdapter.bindToast(uhfDataLayout, "设备连接失败")
                }
                SocketManager.getInstance().releaseRequest()
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