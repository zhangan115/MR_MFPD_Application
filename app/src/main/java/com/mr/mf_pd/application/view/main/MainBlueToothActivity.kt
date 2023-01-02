package com.mr.mf_pd.application.view.main

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.bluetooth.le.*
import android.content.*
import android.net.*
import android.os.*
import android.provider.Settings
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.Nullable
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
import com.mr.mf_pd.application.blueTooth.BtReceiver
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.common.Constants
import com.mr.mf_pd.application.databinding.MainDataBinding
import com.mr.mf_pd.application.manager.socket.SocketManager
import com.mr.mf_pd.application.manager.socket.callback.BytesDataCallback
import com.mr.mf_pd.application.manager.socket.comand.CommandHelp
import com.mr.mf_pd.application.manager.socket.comand.CommandType
import com.mr.mf_pd.application.manager.udp.DeviceListenerManager
import com.mr.mf_pd.application.model.DeviceBean
import com.mr.mf_pd.application.service.BluetoothLeService
import com.mr.mf_pd.application.utils.ZLog
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
import com.sito.tool.library.utils.SPHelper
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_device_check.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.checkDataLayout
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MainBlueToothActivity : AbsBaseActivity<MainDataBinding>() {

    private val viewModel by viewModels<MainViewModel> { getViewModelFactory() }

    private var dataList = ArrayList<DeviceBean>()
    private var scanDataList = HashMap<String, ScanResult>()
    private val REQUEST_ENABLE_BT = 200
    private var mBtReceiver: BtReceiver? = null

    var disposable: Disposable? = null
    var isShowTips = false
    var mServiceConnectionState = false
    var connected = false
    var mBleAddress: String? = null

    private var bluetoothService: BluetoothLeService? = null

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName?, service: IBinder?) {
            ZLog.d(TAG, "onServiceConnected")
            bluetoothService = (service as BluetoothLeService.LocalBinder).getService()
            bluetoothService?.let { bluetooth ->
                if (!bluetooth.initialize()) {
                    ZLog.e(TAG, "Unable to initialize Bluetooth")
                    mServiceConnectionState = false
                    finish()
                }
                mServiceConnectionState = true
            }
        }

        override fun onServiceDisconnected(cn: ComponentName?) {
            ZLog.d(TAG, "onServiceDisconnected")
            bluetoothService = null
            mServiceConnectionState = false
        }
    }

    private val gattUpdateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            ZLog.d(TAG, "onReceive action = " + intent.action)
            when (intent.action) {
                BluetoothLeService.ACTION_GATT_CONNECTED -> {
                    connected = true
                    updateConnectionState()
                }
                BluetoothLeService.ACTION_GATT_DISCONNECTED -> {
                    connected = false
                    updateConnectionState()
                }
                BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED -> {
                    displayGattServices(bluetoothService?.getSupportedGattServices())
                }
            }
        }
    }

    private fun displayGattServices(gattServices: List<BluetoothGattService>?) {
        if (gattServices == null) {
            ZLog.e(TAG, "displayGattServices services is null.")
            return
        }
        val gattService =
            gattServices.find { it.uuid.equals(UUID.fromString("0000a002-0000-1000-8000-00805f9b34fb")) }
        gattService?.let { service ->
            service.characteristics.forEach {
                ZLog.d(TAG, "characteristics uuid = " + it.uuid)
                when(it.uuid){
                    bluetoothService?.notifyCharacterUuid->{
                        bluetoothService?.setCharacteristicNotification(it,true)
                    }
                    bluetoothService?.writeCharacterUuid->{
                        bluetoothService?.setWriteCharacteristic(it)
                        //bluetoothService?.writeData(CommandHelp.getTimeCommand())
                    }
                }
            }
        }
        bluetoothService?.writeData(CommandHelp.switchPassageway(0,1))
    }

    private fun connectBleDevice(deviceAddress: String?) {
        ZLog.d(TAG,
            "mServiceConnectionState = $mServiceConnectionState deviceAddress = $deviceAddress")
        if (mServiceConnectionState && !TextUtils.isEmpty(deviceAddress)) {
            bluetoothService?.connect(deviceAddress!!)
            mBleAddress = deviceAddress
        }
    }

    private fun updateConnectionState() {
        val bleDevice = dataList.find { it.serialNo.equals(mBleAddress) }
        bleDevice?.let {
            if (connected) {
                it.linkState = 1
                it.linkStateStr = "已连接"
            } else {
                it.linkState = 0
                it.linkStateStr = "未连接"
            }
        }
        recycleView.adapter?.notifyDataSetChanged()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val gattServiceIntent = Intent(this, BluetoothLeService::class.java)
        bindService(gattServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        createNotificationChannel()
        //DeviceListenerManager.startListener()
        SocketManager.get().release()
        /*DeviceListenerManager.addListener(object : UDPListener {
            override fun onData(byteArray: ByteArray) {
                ZLog.i(TAG, byteArray.toHexString())
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
//                    val power = (Math.random() * 100).toInt()
                    val power = devicePowerByteArray.first().toInt()
                    val powerState = if (power <= 20) 0 else 1
                    val device = if (deviceMap.containsKey(deviceNum)) {
                        deviceMap[deviceNum]
                    } else {
                        DeviceBean(mrPDStr, deviceNum, 0, power, powerState, null, 0, ip)
                    }
                    if (device != null) {
                        device.power = power
                        device.ip = ip
                        device.powerAttr.set(String.format("%d", device.power) + "%")
                        device.powerStateAttr.set(powerState)
                        ZLog.i(TAG, "device power is $power ip = $ip powerState = $powerState")
                        deviceMap[deviceNum] = device
                        if (!dataList.contains(device)) {
                            dataList.add(device)
                        }
                        runOnUiThread {
                            viewModel.deviceExist.postValue(dataList.isNotEmpty())
                            recycleView.adapter?.notifyDataSetChanged()
                        }
                    }
                    if (device == null || SocketManager.get().getConnection()) {
                        return
                    }
                    runOnUiThread {
                        linkToDevice(device)
                        showNotification(power)
                    }
                }
            }

            override fun onError() {
                viewModel.toastStr.value = "搜索设备失败！"
            }

            override fun onStart() {
                viewModel.toastStr.value = "开始搜索设备！"
            }
        })*/
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
            ZLog.d(TAG, "device = $device")
            connectBleDevice(device.serialNo)
//            if (SocketManager.get().linkedDeviceSerialNo != null) {
//                if (dataList[position].serialNo == SocketManager.get().linkedDeviceSerialNo) {
//                    openCheckActivity(device)
//                } else {
//                    linkToDevice(device)
//                }
//            } else {
//                linkToDevice(device)
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
            requestData()
        }
        scanBtn.setOnClickListener {
            refreshLayout.autoRefresh()
        }
        refreshLayout.setEnableRefresh(true)
        refreshLayout.setEnableLoadMore(false)
    }

    override fun initData(savedInstanceState: Bundle?) {
        dataBinding.vm = viewModel
        viewModel.start()
    }

    private fun startScanBlueDevice() {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        if (adapter == null) {
            Toast.makeText(this, "当前设备不支持蓝牙", Toast.LENGTH_SHORT).show()
            return
        }
        if (!adapter.isEnabled) {
            //打开蓝牙
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            return
        }
        bluetoothLeScanner = adapter.bluetoothLeScanner
        if (bluetoothLeScanner == null) {
            Toast.makeText(this, "不支持当前设备", Toast.LENGTH_LONG).show()
            return
        }
        scanLeDevice()
    }

    private var scanning = false
    private val handler = Handler()
    private var bluetoothLeScanner: BluetoothLeScanner? = null
    private val SCAN_PERIOD: Long = 10000

    private fun scanLeDevice() {
        if (!scanning) {
            handler.postDelayed({
                scanning = false
                refreshLayout.finishRefresh()
                bluetoothLeScanner?.stopScan(leScanCallback)
            }, SCAN_PERIOD)
            scanning = true
            refreshLayout.autoRefresh()
//            val uuid = ParcelUuid.fromString("0000a002-0000-1000-8000-00805f9b34fb")
//            val scanFilter = ScanFilter.Builder().setServiceUuid(uuid).build()
//            val list = ArrayList<ScanFilter>()
//            list.add(scanFilter)
//            val settings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_BALANCED).build()
//            bluetoothLeScanner?.startScan(list,settings,leScanCallback)
            bluetoothLeScanner?.startScan(leScanCallback)
        } else {
            scanning = false
            refreshLayout.finishRefresh()
            bluetoothLeScanner?.stopScan(leScanCallback)
        }
    }

    private val leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            val name = result.scanRecord?.deviceName
            ZLog.d(TAG, "ScanResult device name = $name")
            if (name.equals("PDM-H600")) {
                ZLog.d(TAG, result.toString())
                scanDataList[result.device.address] = result
                updateBlueDeviceList()
            }
        }
    }

    override fun getContentView(): Int {
        return R.layout.activity_main
    }

    override fun requestData() {
        SoulPermission.getInstance().checkAndRequestPermissions(
            Permissions.build(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ),
            object : CheckRequestPermissionsListener {

                override fun onAllPermissionOk(allPermissions: Array<Permission?>) {
                    startScanBlueDevice()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        checkExternalStorageState()
                    } else {
                        val file = MRApplication.instance.fileCacheFile()
                        if (file != null && file.exists()) {
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK) {
            startScanBlueDevice()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
        SocketManager.get().removeCallBack(CommandType.SendTime, sendTimeCallback)
        SocketManager.get().release()
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
            if (file != null && file.exists()) {
                file.mkdir()
            }
            file?.let {
                initLog(it)
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
                    lifecycleOwner(this@MainBlueToothActivity)
                }
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter())
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(gattUpdateReceiver)
    }

    private fun initLog(file: File) {
        val openLog = SPHelper.readBoolean(MRApplication.instance.applicationContext,
            ConstantStr.USER,
            ConstantStr.USER_CONFIG_LOG_OPEN,
            false)
        ZLog.init(openLog, file)
    }

    private fun makeGattUpdateIntentFilter(): IntentFilter {
        return IntentFilter().apply {
            addAction(BluetoothLeService.ACTION_GATT_CONNECTED)
            addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED)
            addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED)
        }
    }

    private fun updateBlueDeviceList() {
        dataList.clear()
        scanDataList.forEach { (_, bluetoothDevice) ->
            val device =
                DeviceBean(bluetoothDevice.device.name,
                    bluetoothDevice.device.address,
                    0,
                    0,
                    0,
                    null,
                    0,
                    "")
            dataList.add(device)
            ZLog.d(TAG, "device = $device")
        }
        viewModel.deviceExist.postValue(dataList.isNotEmpty())
        recycleView.adapter?.notifyDataSetChanged()
    }

}