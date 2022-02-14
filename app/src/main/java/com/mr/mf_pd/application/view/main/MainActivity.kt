package com.mr.mf_pd.application.view.main

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.*
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import android.os.Bundle
import android.os.PatternMatcher
import android.util.Log
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.mr.mf_pd.application.BR
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.adapter.GenericQuickAdapter
import com.mr.mf_pd.application.app.MRApplication
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.databinding.MainDataBinding
import com.mr.mf_pd.application.manager.socket.CommandHelp
import com.mr.mf_pd.application.manager.socket.SocketManager
import com.mr.mf_pd.application.manager.wifi.BaseWiFiManager
import com.mr.mf_pd.application.manager.wifi.WiFiManager
import com.mr.mf_pd.application.manager.wifi.listener.OnWifiConnectListener
import com.mr.mf_pd.application.manager.wifi.listener.OnWifiEnabledListener
import com.mr.mf_pd.application.manager.wifi.listener.OnWifiScanResultsListener
import com.mr.mf_pd.application.model.DeviceBean
import com.mr.mf_pd.application.utils.ByteUtil
import com.mr.mf_pd.application.utils.NumberUtils
import com.mr.mf_pd.application.utils.getViewModelFactory
import com.mr.mf_pd.application.view.base.AbsBaseActivity
import com.mr.mf_pd.application.view.check.DeviceCheckActivity
import com.mr.mf_pd.application.view.file.FilePickerActivity
import com.mr.mf_pd.application.view.setting.SettingActivity
import com.mr.mf_pd.application.view.task.TaskActivity
import com.qw.soul.permission.SoulPermission
import com.qw.soul.permission.bean.Permission
import com.qw.soul.permission.bean.Permissions
import com.qw.soul.permission.callbcak.CheckRequestPermissionsListener
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AbsBaseActivity<MainDataBinding>(),
    OnWifiConnectListener, OnWifiEnabledListener, OnWifiScanResultsListener {

    private val viewModel by viewModels<MainViewModel> { getViewModelFactory() }
    private var mWiFiManager: WiFiManager? = null
    private var dataList = ArrayList<DeviceBean>()
    private var scanDataList = ArrayList<ScanResult>()
    private var linkPosition = -1

    private val wifiReceiver = WiFiManager.NetworkBroadcastReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
        intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)
        registerReceiver(wifiReceiver, intentFilter)
    }

    override fun initView(savedInstanceState: Bundle?) {
        val adapter = GenericQuickAdapter(
            R.layout.item_device_list, this.dataList, BR.deviceBean
        )
        recycleView.layoutManager = LinearLayoutManager(this)
        recycleView.adapter = adapter
        adapter.addChildClickViewIds(R.id.layout_item_root)
        adapter.setOnItemChildClickListener { _, _, position ->
            if (dataList[position].deviceName.equals("Test")) {
                socketLink(position)
                return@setOnItemChildClickListener
            }
            if (mWiFiManager != null && mWiFiManager!!.connectionInfo?.bssid.equals(scanDataList[position].BSSID)) {
                socketLink(position)
                return@setOnItemChildClickListener
            } else {
                dataList[position].deviceName?.let {
                    linkPosition = position
                    //API 29 以上无法直接连接WIFI需要单独处理
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        connectWifi()
                    } else {
                        mWiFiManager?.connectOpenNetwork(it)
                    }
                }
            }
        }
        checkDataLayout.setOnClickListener {
            val intent = Intent(this, FilePickerActivity::class.java)
            startActivity(intent)
        }
        checkTaskLayout.setOnClickListener {
            val intent = Intent(this, TaskActivity::class.java)
            startActivity(intent)
        }
        settingLayout.setOnClickListener {
            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)
        }
        refreshLayout.setOnRefreshListener {
            mWiFiManager?.startScan()
        }
        scanBtn.setOnClickListener {
            refreshLayout.autoRefresh()
        }
        refreshLayout.setEnableRefresh(false)
        refreshLayout.setEnableLoadMore(false)
    }

    override fun initData(savedInstanceState: Bundle?) {
        dataBinding.vm = viewModel
        viewModel.start()
    }

    override fun getContentView(): Int {
        return R.layout.activity_main
    }

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
                            mWiFiManager?.openWiFi()//打开wifi
                        }
                        refreshLayout.setEnableRefresh(true)
                        // 添加监听
                        mWiFiManager?.setOnWifiConnectListener(this@MainActivity)
                        mWiFiManager?.setOnWifiScanResultsListener(this@MainActivity)
                        mWiFiManager?.setOnWifiEnabledListener(this@MainActivity)
                        mWiFiManager?.startScan()
                    }
                }

                override fun onPermissionDenied(refusedPermissions: Array<Permission>) {
                    viewModel.toastStr.postValue("权限申请失败，请去设置页面手动开启权限")
                }
            })
    }

    /**
     * 打开检测页面
     * @param position 选择的WIFI 位置
     */
    private fun socketLink(position: Int) {
        linkPosition = position
        val intent = Intent(this, DeviceCheckActivity::class.java)
        intent.putExtra(ConstantStr.KEY_BUNDLE_OBJECT, dataList[position])
        startActivity(intent)
    }

    private var connectivityManager: ConnectivityManager? = null

    /**
     * Android Q 以上版本连接WIFI
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun connectWifi() {
        val builder = WifiNetworkSpecifier.Builder()
        builder.setSsidPattern(
            PatternMatcher(
                scanDataList[linkPosition].SSID,
                PatternMatcher.PATTERN_PREFIX
            )
        ).setBssidPattern(
            MacAddress.fromString(scanDataList[linkPosition].BSSID),
            MacAddress.fromString("ff:ff:ff:ff:ff:ff")
        )
        val wifiNetworkSpecifier = builder.build()
        val networkRequestBuilder = NetworkRequest.Builder()
        networkRequestBuilder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        networkRequestBuilder.addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)
        networkRequestBuilder.addCapability(NetworkCapabilities.NET_CAPABILITY_TRUSTED)
        networkRequestBuilder.setNetworkSpecifier(wifiNetworkSpecifier)
        val networkRequest = networkRequestBuilder.build()
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager?.requestNetwork(networkRequest, callback)
    }

    val callback = object : ConnectivityManager.NetworkCallback() {
        @Override
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
        }

        @Override
        override fun onUnavailable() {
            super.onUnavailable()
            viewModel.toastStr.postValue("WIFI连接失败")
        }
    }

    override fun onWifiEnabled(enabled: Boolean) {
        //wifi 打开后开启WIFI扫描
        if (enabled) {
            mWiFiManager?.startScan()
        }
    }

    override fun onScanResults(scanResults: MutableList<ScanResult>?) {
        val wifiList = BaseWiFiManager.excludeRepetition(scanResults)
        dataList.clear()
        scanDataList.clear()
        wifiList.forEach {
            if (it.SSID != null && it.capabilities.equals("[ESS]")) {
                val deviceBean = DeviceBean(
                    it.SSID!!, "", it.level,
                    0, 0, "", 0, it.BSSID
                )
                dataList.add(deviceBean)
                scanDataList.add(it)
            }
        }
        val deviceBean = DeviceBean(
            "Test", "", -80,
            0, 0, "", 0, "BSSID"
        )
        dataList.add(deviceBean)

        viewModel.deviceExist.postValue(dataList.isNotEmpty())
        recycleView.adapter?.notifyDataSetChanged()
        refreshLayout.finishRefresh(1000)
    }

    override fun onWiFiConnectSuccess(SSID: String?) {
        if (linkPosition != -1) {
            socketLink(linkPosition)
        }
    }

    override fun onWiFiConnectLog(log: String?) {}


    override fun onWiFiConnectFailure(SSID: String?) {
        viewModel.toastStr.postValue("连接失败")
    }

    private var currentTime: Long = 0

    override fun onBackPressed() {
//        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
//            drawerLayout.closeDrawer(GravityCompat.START)
//            return
//        }
        if (currentTime == 0L) {
            viewModel.toastStr.postValue("再次点击退出App")
            currentTime = System.currentTimeMillis()
        } else {
            if (System.currentTimeMillis() - currentTime >= 2000) {
                currentTime = 0L
                viewModel.toastStr.postValue("再次点击退出App")
            } else {
                MRApplication.instance.exitApp()
                super.onBackPressed()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mWiFiManager?.removeOnWifiScanResultsListener()
        mWiFiManager?.removeOnWifiConnectListener()
        SocketManager.getInstance().releaseRequest()
        unregisterReceiver(wifiReceiver)
        connectivityManager?.unregisterNetworkCallback(callback)
    }
}