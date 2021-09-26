package com.mr.mf_pd.application.view.main

import android.Manifest
import android.content.Intent
import android.net.wifi.ScanResult
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.kongqw.wifilibrary.BaseWiFiManager
import com.kongqw.wifilibrary.WiFiManager
import com.kongqw.wifilibrary.listener.OnWifiConnectListener
import com.kongqw.wifilibrary.listener.OnWifiEnabledListener
import com.kongqw.wifilibrary.listener.OnWifiScanResultsListener
import com.mr.mf_pd.application.BR
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.adapter.GenericQuickAdapter
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.common.Constants
import com.mr.mf_pd.application.databinding.MainDataBinding
import com.mr.mf_pd.application.manager.SocketManager
import com.mr.mf_pd.application.model.DeviceBean
import com.mr.mf_pd.application.utils.getViewModelFactory
import com.mr.mf_pd.application.view.base.AbsBaseActivity
import com.mr.mf_pd.application.view.main.check.DeviceCheckActivity
import com.qw.soul.permission.SoulPermission
import com.qw.soul.permission.bean.Permission
import com.qw.soul.permission.bean.Permissions
import com.qw.soul.permission.callbcak.CheckRequestPermissionsListener
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AbsBaseActivity<MainDataBinding>(), OnWifiScanResultsListener,
    OnWifiConnectListener {

    private val viewModel by viewModels<MainViewModel> { getViewModelFactory() }
    var dataList = ArrayList<DeviceBean>()
    private var linkPosition = -1;

    override fun initView(savedInstanceState: Bundle?) {
        val adapter = GenericQuickAdapter(
            R.layout.item_device_list, this.dataList, BR.deviceBean
        )
        recycleView.layoutManager = LinearLayoutManager(this)
        recycleView.adapter = adapter
        adapter.addChildClickViewIds(R.id.layout_item_root)
        adapter.setOnItemChildClickListener { _, _, position ->
            dataList[position].deviceName?.let {
                linkPosition = position
                mWiFiManager?.connectOpenNetwork(it)
            }
        }
        checkDataLayout.setOnClickListener {

        }
        checkTaskLayout.setOnClickListener {

        }
        settingLayout.setOnClickListener {

        }
        refreshLayout.setOnRefreshListener {
            scanWifiList()
        }
        refreshLayout.setEnableRefresh(false)
        refreshLayout.setEnableLoadMore(false)
    }

    private fun socketLink(position: Int) {
        SocketManager.getInstance().releaseRequest()
        SocketManager.getInstance().initLink()
        SocketManager.getInstance().addLinkStateListeners {
            if (it == Constants.LINK_SUCCESS) {
                viewModel.toastStr.postValue("连接成功")
                val intent = Intent(this, DeviceCheckActivity::class.java)
                intent.putExtra(ConstantStr.KEY_BUNDLE_OBJECT, dataList[position])
                startActivity(intent)
            } else {
                viewModel.toastStr.postValue("连接失败")
            }
        }
    }

    override fun initData(savedInstanceState: Bundle?) {
        dataBinding.vm = viewModel
        viewModel.start()
    }

    override fun getContentView(): Int {
        return R.layout.activity_main
    }

    private var mWiFiManager: WiFiManager? = null

    override fun requestData() {
        SoulPermission.getInstance().checkAndRequestPermissions(
            Permissions.build(
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_MULTICAST_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ),
            object : CheckRequestPermissionsListener {

                override fun onAllPermissionOk(allPermissions: Array<Permission?>) {
                    mWiFiManager = WiFiManager.getInstance(applicationContext)
                    if (mWiFiManager != null) {
                        if (!mWiFiManager!!.isWifiEnabled) {//wifi是否打开状态
                            mWiFiManager!!.openWiFi()//打开wifi
                        }
                        // 添加监听
                        mWiFiManager?.setOnWifiConnectListener(this@MainActivity)
                        refreshLayout.setEnableRefresh(true)
                        scanWifiList()
                    }
                }

                override fun onPermissionDenied(refusedPermissions: Array<Permission>) {

                }
            })
    }


    private fun scanWifiList() {
        mWiFiManager!!.startScan()
        mWiFiManager!!.setOnWifiScanResultsListener(this@MainActivity)
        val allWifiList = mWiFiManager!!.scanResults
        val wifiList = BaseWiFiManager.excludeRepetition(allWifiList)
        dataList.clear()
        wifiList.forEach {
            if (it.SSID != null && it.capabilities.equals("[ESS]")) {
                val deviceBean = DeviceBean(it.SSID!!, "", it.level, 0, 0, "", 0)
                dataList.add(deviceBean)
            }
        }
        recycleView.adapter?.notifyDataSetChanged()
        refreshLayout.finishRefresh(1000)
    }

    override fun onDestroy() {
        super.onDestroy()
        mWiFiManager?.removeOnWifiScanResultsListener()
        mWiFiManager?.removeOnWifiConnectListener()
        SocketManager.getInstance().releaseRequest()
    }

    override fun onScanResults(scanResults: MutableList<ScanResult>?) {
        Log.d("za", "scanResults $scanResults")
    }

    override fun onWiFiConnectLog(log: String?) {
        Log.e("za", "onWiFiConnectLog $log")
    }

    override fun onWiFiConnectSuccess(SSID: String?) {
        Log.e("za", "onWiFiConnectSuccess $SSID")
        if (linkPosition != -1) {
            socketLink(linkPosition)
        }
    }

    override fun onWiFiConnectFailure(SSID: String?) {
        Log.e("za", "onWiFiConnectFailure $SSID")
    }

}