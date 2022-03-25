package com.mr.mf_pd.application.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.mr.mf_pd.application.app.MRApplication
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.manager.file.CheckFileReadManager
import com.mr.mf_pd.application.model.CheckParamsBean
import com.mr.mf_pd.application.model.SettingBean
import com.mr.mf_pd.application.repository.callback.ReadSettingCallback
import com.mr.mf_pd.application.repository.impl.FilesRepository
import com.mr.mf_pd.application.utils.DateUtil
import com.mr.mf_pd.application.utils.FileUtils
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

    override fun getCheckType(): CheckType {
        return mCheckType
    }

    override fun releaseReadFile() {
        CheckFileReadManager.get().releaseReadFile()
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