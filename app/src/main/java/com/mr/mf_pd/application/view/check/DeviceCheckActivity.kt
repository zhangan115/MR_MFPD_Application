package com.mr.mf_pd.application.view.check

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.adapter.ToastAdapter
import com.mr.mf_pd.application.annotation.ClickAnnotationRealize
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.common.Constants
import com.mr.mf_pd.application.databinding.DeviceCheckDataBinding
import com.mr.mf_pd.application.manager.socket.comand.CommandHelp
import com.mr.mf_pd.application.manager.socket.SocketManager
import com.mr.mf_pd.application.manager.socket.callback.BytesDataCallback
import com.mr.mf_pd.application.model.DeviceBean
import com.mr.mf_pd.application.view.base.AbsBaseActivity
import com.mr.mf_pd.application.view.file.FilePickerActivity
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_device_check.*

class DeviceCheckActivity : AbsBaseActivity<DeviceCheckDataBinding>() {

    private var mDeviceBean: DeviceBean? = null

    override fun initView(savedInstanceState: Bundle?) {
        uhfDataLayout.setOnClickListener {
            val intent = Intent(this, CheckDataActivity::class.java)
            intent.putExtra(ConstantStr.KEY_BUNDLE_OBJECT, CheckType.UHF)
            startActivity(intent)
        }
        aeTaskLayout.setOnClickListener {
            val intent = Intent(this, CheckDataActivity::class.java)
            intent.putExtra(ConstantStr.KEY_BUNDLE_OBJECT, CheckType.AE)
            startActivity(intent)
        }
        tevDataLayout.setOnClickListener {
            val intent = Intent(this, CheckDataActivity::class.java)
            intent.putExtra(ConstantStr.KEY_BUNDLE_OBJECT, CheckType.TEV)
            startActivity(intent)
        }
        hfTaskLayout.setOnClickListener {
            val intent = Intent(this, CheckDataActivity::class.java)
            intent.putExtra(ConstantStr.KEY_BUNDLE_OBJECT, CheckType.HF)
            startActivity(intent)
        }
        checkDataLayout.setOnClickListener {
            val intent = Intent(this, FilePickerActivity::class.java)
            startActivity(intent)
        }
        selfCheckingLayout.visibility = View.GONE
        selfCheckingLayout.setOnClickListener {

        }
        linkToDevice()
        ClickAnnotationRealize.Bind(this)
    }

    var disposable: Disposable? = null
    var isShowTips = false

    private fun linkToDevice() {
        SocketManager.get().releaseRequest()
        SocketManager.get().initLink()
        SocketManager.get().addLinkStateListeners {
            if (it == Constants.LINK_SUCCESS) {
                //一分钟一次的连接对时，保证数据接通
                disposable = SocketManager.get().sendRepeatData(CommandHelp.getTimeCommand(), 60)
            } else {
                runOnUiThread {
                    ToastAdapter.bindToast(uhfDataLayout, "设备连接失败")
                }
                SocketManager.get().releaseRequest()
            }
        }
        SocketManager.get().sendTimeCallback = object : BytesDataCallback {
            override fun onData(source: ByteArray) {
                if (isShowTips) {
                    return
                }
                isShowTips = true
                //解决不断上传的问题，对时成功后先关闭采集通道
                val close = CommandHelp.closePassageway()
                SocketManager.get().sendData(close)
                runOnUiThread {
                    ToastAdapter.bindToast(uhfDataLayout, "设备连接成功")
                }
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
        disposable?.dispose()
        SocketManager.get().releaseRequest()
    }
}