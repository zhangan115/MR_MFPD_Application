package com.mr.mf_pd.application.repository

import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.mr.mf_pd.application.app.MRApplication
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.manager.file.CheckFileReadManager
import com.mr.mf_pd.application.manager.socket.SocketManager
import com.mr.mf_pd.application.model.CheckParamsBean
import com.mr.mf_pd.application.model.SettingBean
import com.mr.mf_pd.application.repository.callback.ReadSettingCallback
import com.mr.mf_pd.application.repository.impl.FilesRepository
import com.mr.mf_pd.application.utils.DateUtil
import com.mr.mf_pd.application.utils.FileUtils
import com.mr.mf_pd.application.view.file.model.CheckConfigModel
import com.mr.mf_pd.application.view.file.model.CheckDataFileModel
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

class DefaultFilesRepository : FilesRepository {

    var isSaving: MutableLiveData<Boolean> = MutableLiveData(false)

    companion object {
        var realDataMaxValue: MutableLiveData<Int> = MutableLiveData()
        var realDataMinValue: MutableLiveData<Int> = MutableLiveData()
    }

    private var checkTempFile: File? = null
    var checkFile: File? = null

    private var startTime = 0L
    private var endTime = 0L

    private lateinit var mCheckType: CheckType
    var checkParamsBean: CheckParamsBean? = null

    override fun startSaveData() {
        isSaving.postValue(true)
        val tempDir = File(MRApplication.instance.cacheDir, "temp")
        if (!tempDir.exists()) {
            tempDir.mkdir()
        }
        checkTempFile =
            File(tempDir, DateUtil.timeFormat(System.currentTimeMillis(), "yyyy_MM_dd_HH_mm_ss"))
        if (!checkTempFile!!.exists()) {
            checkTempFile!!.mkdir()
        }
        SocketManager.get().setSaveDataFile(checkTempFile)
        startTime = System.currentTimeMillis()
    }

    private var flightRowCount = 0
    private var pulseRowCount = 0
    private var ycRowCount = 0
    private var realRowCount = 0

    override fun stopSaveData() {
        isSaving.postValue(false)
        SocketManager.get().stopSaveData()

        this.flightRowCount = SocketManager.get().flightRowCount
        this.pulseRowCount = SocketManager.get().pulseRowCount
        this.ycRowCount = SocketManager.get().ycRowCount
        this.realRowCount = SocketManager.get().realRowCount

        endTime = System.currentTimeMillis()

    }

    override fun setCurrentClickFile(file: File) {
        checkFile = file
        MRApplication.instance.saveCheckFileToSp(file)
    }

    override fun toCreateCheckFile(checkType: CheckType) {
        if (checkFile != null) {
            GlobalScope.runCatching {
                try {
                    val g = Gson()
                    val checkFileName = checkTempFile!!.name
                    val checkFile = File(checkFile!!, checkFileName)
                    checkFile.mkdir()
                    //创建配置信息
                    val configBean = CheckConfigModel()
                    configBean.type = checkType.checkFile
                    configBean.time = endTime - startTime
                    configBean.flightRowCount = flightRowCount
                    configBean.pulseRowCount = flightRowCount
                    configBean.realRowCount = realRowCount
                    configBean.ycRowCount = ycRowCount

                    val configStr = g.toJson(configBean)
                    FileUtils.writeStr2File(
                        configStr,
                        File(checkFile, ConstantStr.CHECK_FILE_CONFIG)
                    )
                    //保存设置
                    val settingStr = g.toJson(checkType.settingBean)
                    FileUtils.writeStr2File(
                        settingStr,
                        File(checkFile, ConstantStr.CHECK_FILE_SETTING)
                    )
                    if (checkTempFile?.exists() == true) {
                        checkTempFile?.listFiles()?.toList()?.forEach {
                            FileUtils.copyFile(it, File(checkFile,it.name))
                        }
                    }
                    FileUtils.deleteFile(checkTempFile)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun getCurrentCheckFile(): File? {
        return checkFile
    }

    override fun getCurrentCheckName(): String? {
        checkFile?.let {
            val str = it.absolutePath.removeRange(
                0,
                MRApplication.instance.fileCacheFile()!!.absolutePath.length
            )
            if (TextUtils.isEmpty(str)) {
                return "/"
            }
            return str
        }
        return null
    }

    override fun isSaveData(): MutableLiveData<Boolean> {
        return isSaving
    }

    override fun getCheckType(): CheckType {
        return mCheckType
    }

    override fun releaseReadFile() {
        CheckFileReadManager.get().releaseReadFile()
    }

    private var checkDataFileModel: CheckDataFileModel? = null

    override fun setCheckFileModel(model: CheckDataFileModel?) {
        checkDataFileModel = model
    }

    override fun getCheckFileModel(): CheckDataFileModel? {
        return checkDataFileModel
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
        CheckFileReadManager.get().setFile(file)
        return Observable.create { emitter: ObservableEmitter<SettingBean?> ->
            try {
                val str = FileUtils.readStrFromFile(CheckFileReadManager.get().settingFile)
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

}