package com.mr.mf_pd.application.view.main

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.*
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.PatternMatcher
import android.provider.Settings
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.mr.mf_pd.application.BR
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.adapter.GenericQuickAdapter
import com.mr.mf_pd.application.app.MRApplication
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.common.Constants
import com.mr.mf_pd.application.databinding.MainDataBinding
import com.mr.mf_pd.application.manager.socket.SocketManager
import com.mr.mf_pd.application.manager.socket.callback.BytesDataCallback
import com.mr.mf_pd.application.manager.socket.comand.CommandHelp
import com.mr.mf_pd.application.manager.socket.comand.CommandType
import com.mr.mf_pd.application.manager.udp.DeviceListenerManager
import com.mr.mf_pd.application.manager.udp.UDPListener
import com.mr.mf_pd.application.manager.wifi.BaseWiFiManager
import com.mr.mf_pd.application.manager.wifi.WiFiManager
import com.mr.mf_pd.application.manager.wifi.listener.OnWifiConnectListener
import com.mr.mf_pd.application.manager.wifi.listener.OnWifiEnabledListener
import com.mr.mf_pd.application.manager.wifi.listener.OnWifiScanResultsListener
import com.mr.mf_pd.application.model.DeviceBean
import com.mr.mf_pd.application.utils.ByteUtil
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
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_device_check.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.checkDataLayout
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MainActivity : AbsBaseActivity<MainDataBinding>(),
    OnWifiConnectListener, OnWifiEnabledListener, OnWifiScanResultsListener {

    private val viewModel by viewModels<MainViewModel> { getViewModelFactory() }
    private var mWiFiManager: WiFiManager? = null
    private var dataList = ArrayList<DeviceBean>()
    private var scanDataList = ArrayList<ScanResult>()
    private val deviceMap = HashMap<String, DeviceBean>()
    private var linkPosition = -1

    private val wifiReceiver = WiFiManager.NetworkBroadcastReceiver()

    var disposable: Disposable? = null
    var isShowTips = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
        intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)
        registerReceiver(wifiReceiver, intentFilter)
        DeviceListenerManager.startListener()
        SocketManager.get().release()
        DeviceListenerManager.addListener(object : UDPListener {
            override fun onData(byteArray: ByteArray) {
                val mrPDByteArray = ByteArray(4)
                val ipAddressByteArray = ByteArray(4)
                val deviceNumByteArray = ByteArray(6)
                val devicePowerByteArray = ByteArray(1)
                System.arraycopy(byteArray, 0, mrPDByteArray, 0, 4)
                System.arraycopy(byteArray, 4, ipAddressByteArray, 0, 4)
                System.arraycopy(byteArray, 8, deviceNumByteArray, 0, 6)
                System.arraycopy(byteArray, 14, devicePowerByteArray, 0, 1)

                val mrPDStr = String(mrPDByteArray)
                if (mrPDStr == "MRPD") {
                    val deviceNum = ByteUtil.bytes2HexStr(deviceNumByteArray)
                    val intList = IntArray(4)
                    ipAddressByteArray.forEachIndexed { index, byte ->
                        intList[index] = byte.toInt() and 0xff
                    }
                    val ip = intList.joinToString(".")
                    val power = devicePowerByteArray.first().toInt()
                    val device = if (deviceMap.containsKey(deviceNum)) {
                        deviceMap[deviceNum]
                    } else {
                        val powerState =
                            if (power < 20) 0 else if (power in 20..59) 1 else 2
                        DeviceBean(mrPDStr, deviceNum, 0, power, powerState, null, 0, ip)
                    }
                    if (device != null) {
                        device.power = power
                        device.ip = ip
                        deviceMap[deviceNum] = device
                        if (!dataList.contains(device)) {
                            dataList.add(device)
                        }
                        viewModel.deviceExist.postValue(dataList.isNotEmpty())
                        recycleView.adapter?.notifyDataSetChanged()
                    }
                    runOnUiThread {
                        showNotification(power)
                    }
                    if (device == null || SocketManager.get().getConnection()) {
                        return
                    }
                    linkToDevice(device)
                }
            }
        })
    }

    override fun initView(savedInstanceState: Bundle?) {
        val adapter = GenericQuickAdapter(
            R.layout.item_device_list, this.dataList, BR.deviceBean
        )
        recycleView.layoutManager = LinearLayoutManager(this)
        recycleView.adapter = adapter
        adapter.addChildClickViewIds(R.id.layout_item_root)
        adapter.setOnItemChildClickListener { _, _, position ->
            val device = dataList[position]
            if (SocketManager.get().linkedDeviceSerialNo != null) {
                if (dataList[position].serialNo == SocketManager.get().linkedDeviceSerialNo) {
                    openCheckActivity(device)
                } else {
                    linkToDevice(device)
                }
            } else {
                linkToDevice(device)
            }

//            if (mWiFiManager != null && mWiFiManager!!.connectionInfo?.bssid.equals(scanDataList[position].BSSID)) {
//                socketLink(position)
//                return@setOnItemChildClickListener
//            } else {
//                dataList[position].deviceName?.let {
//                    linkPosition = position
//                    //API 29 以上无法直接连接WIFI需要单独处理
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                        connectWifi()
//                    } else {
//                        mWiFiManager?.connectOpenNetwork(it)
//                    }
//                }
//            }
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
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ),
            object : CheckRequestPermissionsListener {

                override fun onAllPermissionOk(allPermissions: Array<Permission?>) {
//                    mWiFiManager = WiFiManager.getInstance(applicationContext)
//                    if (mWiFiManager != null) {
//                        if (!mWiFiManager!!.isWifiEnabled) {//wifi是否打开状态
//                            mWiFiManager?.openWiFi()//打开wifi
//                        }
//                        refreshLayout.setEnableRefresh(false)
//                        // 添加监听
//                        mWiFiManager?.setOnWifiConnectListener(this@MainActivity)
//                        mWiFiManager?.setOnWifiScanResultsListener(this@MainActivity)
//                        mWiFiManager?.setOnWifiEnabledListener(this@MainActivity)
//                        mWiFiManager?.startScan()
//
//                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        checkExternalStorageState()
                    }else{
                        val file = MRApplication.instance.fileCacheFile()
                        if (file!=null && file.exists()) {
                            file.mkdir()
                        }
                    }
                }

                override fun onPermissionDenied(refusedPermissions: Array<Permission>) {
                    viewModel.toastStr.postValue("权限申请失败，请去设置页面手动开启权限")
                }
            })
    }

    /**
     * 打开检测页面
     * @param device 检测设备
     */
    private fun openCheckActivity(device: DeviceBean?) {
        val intent = Intent(this, DeviceCheckActivity::class.java)
        intent.putExtra(ConstantStr.KEY_BUNDLE_OBJECT, device)
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
//        dataList.clear()
//        scanDataList.clear()
//        wifiList.forEach {
//            if (it.SSID != null && it.capabilities.equals("[ESS]")) {
//                val deviceBean = DeviceBean(
//                    it.SSID!!, "", it.level,
//                    0, 0, "", 0, it.BSSID
//                )
//                dataList.add(deviceBean)
//                scanDataList.add(it)
//            }
//        }
//        val deviceBean = DeviceBean(
//            "Test", "", -80,
//            0, 0, "", 0, "BSSID"
//        )
//        dataList.add(deviceBean)
//
//        viewModel.deviceExist.postValue(dataList.isNotEmpty())
        recycleView.adapter?.notifyDataSetChanged()
        refreshLayout.finishRefresh(1000)
    }

    override fun onWiFiConnectSuccess(SSID: String?) {
        if (linkPosition != -1) {

        }
    }

    override fun onWiFiConnectLog(log: String?) {}


    override fun onWiFiConnectFailure(SSID: String?) {
        viewModel.toastStr.postValue("连接失败")
    }

    private var currentTime: Long = 0

    override fun onBackPressed() {
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
        disposable?.dispose()
        SocketManager.get().removeCallBack(CommandType.SendTime, sendTimeCallback)
        SocketManager.get().release()
        unregisterReceiver(wifiReceiver)
        connectivityManager?.unregisterNetworkCallback(callback)
        DeviceListenerManager.disableListener()
    }

    private fun linkToDevice(device: DeviceBean) {
        SocketManager.get().release()
        initSocketCallback()
        SocketManager.get().initLink(device.serialNo, device.ip)
    }

    /**
     * 初始化Socket 连接回调
     */
    private fun initSocketCallback() {
        SocketManager.get().addCallBack(CommandType.SendTime, sendTimeCallback)
        SocketManager.get().addLinkStateListeners {
            if (it == Constants.LINK_SUCCESS) {
                //一分钟一次的连接对时，保证数据接通
                disposable = SocketManager.get().sendRepeatData(CommandHelp.getTimeCommand(), 60)
            } else {
                disposable?.dispose()
                SocketManager.get().release()
            }
            runOnUiThread {
                dataList.forEach { deviceBean ->
                    if (deviceBean.serialNo == SocketManager.get().linkedDeviceSerialNo) {
                        deviceBean.linkState = 1
                        deviceBean.linkStateStr = "已连接"
                    } else {
                        deviceBean.linkState = 0
                        deviceBean.linkStateStr = "未连接"
                    }
                }
                recycleView.adapter?.notifyDataSetChanged()
            }
        }
    }

    private val sendTimeCallback = object : BytesDataCallback {
        override fun onData(source: ByteArray) {
            if (isShowTips) {
                return
            }
            isShowTips = true
            //解决不断上传的问题，对时成功后先关闭采集通道
            val close = CommandHelp.closePassageway()
            SocketManager.get().sendData(close)
        }
    }

    private val notificationMap = ArrayList<Int>()

    private fun showNotification(power: Int) {
        if (power > 20) return
        val powerValue =
            if (power in 15..20) 20 else if (power in 10..14) 15 else if (power in 6..9) 10 else 5
        if (notificationMap.contains(powerValue)) {
            return
        }
        val builder = NotificationCompat.Builder(this, Constants.CHANNEL_ID)
            .setContentTitle("设备充电提示")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentText("设备电量低于$powerValue%,请及时给设备充电")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        with(NotificationManagerCompat.from(this)) {
            notify(powerValue, builder.build())
            notificationMap.add(powerValue)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(Constants.CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun checkExternalStorageState() {
        val isHasStoragePermission = Environment.isExternalStorageManager()
        if (isHasStoragePermission) {
            val file = MRApplication.instance.fileCacheFile()
            if (file!=null && file.exists()) {
                file.mkdir()
            }
            return
        }
        if (!isHasStoragePermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            setTheme(R.style.AppTheme_MaterialDialog)
            MaterialDialog(this)
                .show {
                    title = "提示"
                    message(text = "本程序需要您同意允许访问所有文件权限")
                    negativeButton(text = "取消", click = {
                        it.dismiss()
                    })
                    positiveButton(text = "确定", click = {
                        val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                        startActivity(intent = intent)
                        it.dismiss()
                    })
                    lifecycleOwner(this@MainActivity)
                }
        }
    }

}