package com.mr.mf_pd.application.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.mr.mf_pd.application.app.MRApplication
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.common.Constants
import com.mr.mf_pd.application.manager.file.ReadFileDataManager
import com.mr.mf_pd.application.manager.socket.callback.BytesDataCallback
import com.mr.mf_pd.application.model.CheckParamsBean
import com.mr.mf_pd.application.model.SettingBean
import com.mr.mf_pd.application.repository.callback.DataCallback
import com.mr.mf_pd.application.repository.callback.ReadSettingCallback
import com.mr.mf_pd.application.repository.callback.RealDataCallback
import com.mr.mf_pd.application.repository.impl.FilesRepository
import com.mr.mf_pd.application.utils.ByteUtil
import com.mr.mf_pd.application.utils.DateUtil
import com.mr.mf_pd.application.utils.FileUtils
import com.mr.mf_pd.application.view.opengl.`object`.PrPsCubeList
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.GlobalScope
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.max
import kotlin.math.min

class DefaultFilesRepository : FilesRepository {

    var isSaving: MutableLiveData<Boolean> = MutableLiveData(false)

    companion object {
        var realDataMaxValue: MutableLiveData<Int> = MutableLiveData()
        var realDataMinValue: MutableLiveData<Int> = MutableLiveData()
    }

    private var realDataFOS: FileOutputStream? = null
    private var ycDataFOS: FileOutputStream? = null

    var realDataEmitter: ObservableEmitter<ByteArray>? = null
    var ycDataEmitter: ObservableEmitter<ByteArray>? = null

    var checkTempFile: File? = null
    var checkFile: File? = null
    var realDataTempFile: File? = null
    var ycTempFile: File? = null

    private lateinit var mCheckType: CheckType
    var checkParamsBean: CheckParamsBean? = null
    var receiverCount = 0
    var mcCount = 0
    var maxValue: Float? = null
    var maxGainValue: Float? = null
    var minValue: Float? = null
    @Volatile
    var gainFloatList = Vector<Float>()

    var gainValue: MutableLiveData<Vector<Float>> = MutableLiveData(Vector())

    private var phaseData: ArrayList<HashMap<Int, Float?>> = ArrayList()

    private var realPointData: ArrayList<HashMap<Int, Float?>> = ArrayList()

    var realData: ArrayList<PrPsCubeList> = ArrayList()

    private var realDataCallbacks: ArrayList<RealDataCallback> = ArrayList()

    private var ycDataCallbacks: ArrayList<BytesDataCallback> = ArrayList()

    override fun startSaveData() {
        isSaving.postValue(true)
        val tempDir = File(MRApplication.instance.cacheDir, "temp")
        if (!tempDir.exists()) {
            tempDir.mkdir()
        }
        checkTempFile = File(tempDir, DateUtil.timeFormat(System.currentTimeMillis(), null))
        if (!checkTempFile!!.exists()) {
            checkTempFile!!.mkdir()
        }

        realDataTempFile = File(checkTempFile, ConstantStr.CHECK_REAL_DATA)
        ycTempFile = File(checkTempFile, ConstantStr.CHECK_YC_FILE_NAME)

        if (!realDataTempFile!!.exists()) {
            realDataTempFile!!.createNewFile()
        }
        if (!ycTempFile!!.exists()) {
            ycTempFile!!.createNewFile()
        }
        realDataFOS = FileOutputStream(realDataTempFile!!, true)
        ycDataFOS = FileOutputStream(ycTempFile!!, true)
        val realObs = ObservableOnSubscribe<ByteArray> {
            realDataEmitter = it
        }
        Observable.create(realObs).doOnNext {
            realDataFOS?.write(it)
            realDataFOS?.flush()
        }.observeOn(Schedulers.io())
            .subscribeOn(Schedulers.io())
            .doOnComplete {
                Log.d(
                    "zhangan",
                    realDataTempFile?.absolutePath.toString() + " file size is " + realDataTempFile?.length()
                )
            }.subscribe()
        val ycObs = ObservableOnSubscribe<ByteArray> {
            ycDataEmitter = it
        }
        Observable.create(ycObs).doOnNext {
            ycDataFOS?.write(it)
            ycDataFOS?.flush()
        }.observeOn(Schedulers.io())
            .subscribeOn(Schedulers.io())
            .doOnComplete {
                Log.d(
                    "zhangan",
                    ycTempFile?.absolutePath.toString() + " file size is " + ycTempFile?.length()
                )
            }.subscribe()
    }

    override fun stopSaveData() {
        isSaving.postValue(false)
        realDataEmitter?.onComplete()
    }

    override fun setCurrentClickFile(file: File) {
        checkFile = file
    }

    override fun toCreateCheckFile(checkType: CheckType) {
        if (ycTempFile != null && realDataTempFile != null && checkFile != null) {
            GlobalScope.runCatching {
                try {
                    val checkFileName = checkType.checkFile + checkTempFile!!.name
                    val checkFile = File(checkFile!!, checkFileName)
                    checkFile.mkdir()
                    //保存设置
                    val settingStr = Gson().toJson(checkType.settingBean)
                    FileUtils.writeStr2File(
                        settingStr,
                        File(checkFile, ConstantStr.CHECK_FILE_SETTING)
                    )
                    //保存实时数据
                    FileUtils.copyFile(realDataTempFile,
                        File(checkFile, ConstantStr.CHECK_REAL_DATA))
                    //保存遥测数据
                    FileUtils.copyFile(ycTempFile, File(checkFile, ConstantStr.CHECK_YC_FILE_NAME))
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun getCurrentCheckFile(): File? {
        if (checkFile != null) {
            return checkFile
        }
        return MRApplication.instance.fileCacheFile()
    }

    override fun getCurrentCheckName(): String? {
        checkFile?.let {
            return it.absolutePath.removeRange(
                0,
                MRApplication.instance.fileCacheFile()!!.absolutePath.length
            )
        }
        return null
    }

    override fun toSaveRealData2File(source: ByteArray) {
        realDataEmitter?.onNext(source)
    }

    override fun toSaveYCData2File(source: ByteArray) {
        ycDataEmitter?.onNext(source)
    }

    override fun isSaveData(): MutableLiveData<Boolean> {
        return isSaving
    }


    override fun addDataListener() {
        ReadFileDataManager.get().setReadListener(realDataListener)
        ReadFileDataManager.get().ycDataCallback = ycDataCallback
    }

    private val ycDataCallback = object : BytesDataCallback {
        override fun onData(source: ByteArray) {
            ycDataCallbacks.forEach {
                it.onData(source)
            }
        }
    }

    override fun getGainValueList(): MutableLiveData<Vector<Float>> {
        return gainValue
    }

    override fun getPhaseData(chartType: Int): ArrayList<HashMap<Int, Float?>> {
        val list = ArrayList<HashMap<Int, Float?>>()
        if (chartType == 0) {
            if (phaseData.isNotEmpty()) {
                list.add(phaseData.removeFirst())
            }
        } else if (chartType == 1) {
            if (realPointData.isNotEmpty()) {
                list.add(realPointData.removeFirst())
            }
        }
        return list
    }

    override fun getCheckType(): CheckType {
        return mCheckType
    }

    private val realDataListener = object : BytesDataCallback {
        override fun onData(source: ByteArray) {
            if (source.isNotEmpty() && source.size > 7) {
                realDataCallbacks.forEach {
                    it.onRealDataChanged(source)
                }
                val bytes = ByteArray(source.size - 7)
                System.arraycopy(source, 5, bytes, 0, source.size - 7)

                val newValueList: ArrayList<Float?> = ArrayList()
                for (j in 0 until Constants.PRPS_COLUMN) {
                    newValueList.add(null)
                }
                val newPointList = HashMap<Int, Float?>()
                for (i in 0 until (bytes.size / 6)) {
                    val values = ByteArray(6)
                    System.arraycopy(bytes, 6 * i, values, 0, 6)
                    val row = values[0].toInt()//周期，暂不使用
                    val column = values[1].toInt()
                    val height = ByteArray(4)
                    System.arraycopy(values, 2, height, 0, 4)
                    val f = ByteUtil.getFloat(height)
                    var value = f
                    maxValue = if (maxValue == null) {
                        f
                    } else {
                        max(f, maxValue!!)
                    }
                    maxGainValue = if (maxGainValue == null) {
                        f
                    } else {
                        max(f, maxGainValue!!)
                    }
                    minValue = if (minValue == null) {
                        f
                    } else {
                        min(f, minValue!!)
                    }
                    //根据设置处理数据
                    val setting = getCheckType().settingBean
                    //处理固定尺度
                    if (setting.gdCd == 1) {
                        if (f > setting.maxValue) {
                            value = setting.maxValue.toFloat()
                        } else if (f < setting.minValue) {
                            value = setting.minValue.toFloat()
                        }
                    } else {
                        if (realDataMaxValue.value != null) {
                            val maxValue = max(realDataMaxValue.value!!, f.toInt())
                            if (maxValue != realDataMaxValue.value!!) {
                                realDataMaxValue.postValue(maxValue)
                            }
                        } else {
                            realDataMaxValue.postValue(setting.maxValue)
                        }
                        if (realDataMinValue.value != null) {
                            val minValue = min(realDataMinValue.value!!, f.toInt())
                            if (minValue != realDataMinValue.value!!) {
                                realDataMinValue.postValue(minValue)
                            }
                        } else {
                            realDataMinValue.postValue(setting.minValue)
                        }
                    }
                    //处理偏移量
                    val py = setting.xwPy
                    val off: Int = if (py in 1..359) {
                        val pyValue = (py / 3.6f).toInt()
                        if (column + pyValue > 99) {
                            column + pyValue - 100
                        } else {
                            column + pyValue
                        }
                    } else {
                        column
                    }
                    if (off < Constants.PRPS_COLUMN && off >= 0) {
                        newValueList[off] = value
                        newPointList[off] = value
                    } else {
                        Log.d("zhangan", "数据相位异常：$column")
                    }
                    mcCount++
                }
                phaseData.add(newPointList)
                realPointData.add(newPointList)
                if (realData.size == Constants.PRPS_ROW) {
                    realData.removeFirst()
                }
                realData.add(PrPsCubeList(newValueList))
                if (receiverCount % 5 == 0) {
                    if (maxGainValue != null) {
                        gainFloatList.add(maxGainValue!!)
                    }
                    if (gainFloatList.size > getCheckType().settingBean.ljTime * 10) {
                        gainFloatList.removeFirst()
                    }
                    gainValue.postValue(gainFloatList)
                    maxGainValue = null
                }
                if (receiverCount == 50) { //一秒钟刷新一次数据
                    if (maxValue != null) {
                        val df1 = DecimalFormat("0.00")
                        checkParamsBean?.fzAttr = "${df1.format(maxValue)}dBm"
                    }
                    checkParamsBean?.mcCountAttr = "${mcCount}个/秒"
                    mCheckType.checkParams.postValue(checkParamsBean)
                    receiverCount = 0
                    mcCount = 0
                    maxValue = null
                } else {
                    ++receiverCount
                }
            }
        }
    }

    override fun addYcDataCallback(callback: BytesDataCallback) {
        ycDataCallbacks.add(callback)
    }

    override fun removeYcDataCallback(callback: BytesDataCallback) {
        ycDataCallbacks.remove(callback)
    }

    override fun releaseReadFile() {
        ReadFileDataManager.get().releaseReadFile()
    }

    override fun cleanData() {
        receiverCount = 0
        mcCount = 0
        gainFloatList.clear()
        phaseData.clear()
        realData.clear()
    }

    override fun openCheckFile(
        checkType: CheckType,
        file: File,
        callback: ReadSettingCallback?,
    ): Disposable {
        mCheckType = checkType
        checkParamsBean = mCheckType.checkParams.value
        realDataMaxValue.postValue(mCheckType.settingBean.maxValue)
        realDataMinValue.postValue(mCheckType.settingBean.minValue)
        checkType.checkParams.postValue(checkParamsBean)
        ReadFileDataManager.get().setFile(file)
        return Observable.create { emitter: ObservableEmitter<SettingBean?> ->
            try {
                val str = FileUtils.readStrFromFile(ReadFileDataManager.get().settingFile)
                val settingBean = Gson().fromJson(str, SettingBean::class.java)
                emitter.onNext(settingBean)
            } catch (e: Exception) {
                e.printStackTrace()
                emitter.onError(e)
            } finally {
                emitter.onComplete()
            }
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe {
            it?.let {
                callback?.onSettingBean(it)
            }
        }
    }

    override fun addHufData(callback: DataCallback) {
        var prPsCube: PrPsCubeList? = null
        if (realData.isNotEmpty()) {
            prPsCube = realData.lastOrNull()
        }
        var map: HashMap<Int, Float?>? = null
        if (phaseData.isNotEmpty()) {
            map = phaseData.lastOrNull()
        }
        if (prPsCube != null && map != null) {
            callback.addData(map, prPsCube)
            if (realData.isNotEmpty()) {
                realData.removeLast()
            }
        }
    }

}