package com.mr.mf_pd.application.view.check

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.adapter.ToastAdapter
import com.mr.mf_pd.application.annotation.ClickAnnotationRealize
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.databinding.DeviceCheckDataBinding
import com.mr.mf_pd.application.manager.socket.SocketManager
import com.mr.mf_pd.application.model.DeviceBean
import com.mr.mf_pd.application.view.base.AbsBaseActivity
import com.mr.mf_pd.application.view.file.FilePickerActivity
import kotlinx.android.synthetic.main.activity_device_check.*

class DeviceCheckActivity : AbsBaseActivity<DeviceCheckDataBinding>() {

    private var mDeviceBean: DeviceBean? = null

    override fun initView(savedInstanceState: Bundle?) {
        uhfDataLayout.setOnClickListener {
            startCheckDataActivity(CheckType.UHF)
        }
        aeTaskLayout.setOnClickListener {
            startCheckDataActivity(CheckType.AE)
        }
        tevDataLayout.setOnClickListener {
            startCheckDataActivity(CheckType.TEV)
        }
        hfTaskLayout.setOnClickListener {
            startCheckDataActivity(CheckType.HF)
        }
        aaTaskLayout.setOnClickListener {
            startCheckDataActivity(CheckType.AA)
        }
        checkDataLayout.setOnClickListener {
            val intent = Intent(this, FilePickerActivity::class.java)
            startActivity(intent)
        }
        selfCheckingLayout.visibility = View.GONE
        selfCheckingLayout.setOnClickListener {

        }
        ClickAnnotationRealize.Bind(this)
    }

    private fun startCheckDataActivity(checkType:CheckType){
        if (SocketManager.get().getConnection()){
            val intent = Intent(this, CheckDataActivity::class.java)
            intent.putExtra(ConstantStr.KEY_BUNDLE_OBJECT, checkType)
            startActivity(intent)
        }else{
            ToastAdapter.bindToast(uhfDataLayout, "请连接设备")
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