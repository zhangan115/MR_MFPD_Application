package com.mr.mf_pd.application.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.common.primitives.Bytes
import com.google.gson.Gson
import com.mr.mf_pd.application.app.MRApplication
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.repository.impl.FilesRepository
import com.mr.mf_pd.application.utils.DateUtil
import com.mr.mf_pd.application.utils.FileUtils
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.Observables
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.*
import java.util.*

class DefaultFilesRepository : FilesRepository {

    var isSaving: MutableLiveData<Boolean> = MutableLiveData(false)
    private var fileOutputStream: FileOutputStream? = null
    var emitter: ObservableEmitter<ByteArray>? = null
    var tempFile: File? = null
    var checkFile: File? = null

    override fun startSaveData() {
        isSaving.postValue(true)
        val fileName = DateUtil.timeFormat(System.currentTimeMillis(), null)
        val tempDir = File(MRApplication.instance.cacheDir, "temp")
        if (!tempDir.exists()) {
            tempDir.mkdir()
        }
        tempFile = File(tempDir, fileName)
        if (!tempFile!!.exists()) {
            tempFile!!.createNewFile()
        }
        fileOutputStream = FileOutputStream(tempFile!!, true)
        val obs = ObservableOnSubscribe<ByteArray> {
            emitter = it
        }
        Observable.create(obs).doOnNext {
            fileOutputStream?.write(it)
            fileOutputStream?.flush()
        }.observeOn(Schedulers.io())
            .subscribeOn(Schedulers.io())
            .doOnComplete {
                Log.d(
                    "zhangan",
                    tempFile?.absolutePath.toString() + " file size is " + tempFile?.length()
                )
            }
            .subscribe()
    }

    override fun stopSaveData() {
        isSaving.postValue(false)
        emitter?.onComplete()
    }

    override fun setCurrentChickFile(file: File) {
        checkFile = file
    }

    override fun toCreateCheckFile(checkType: CheckType, ycData: ByteArray?) {
        if (tempFile != null && checkFile != null) {
            GlobalScope.runCatching {
                try {
                    val checkFileName = checkType.checkFile + tempFile!!.name
                    val checkFile = File(checkFile!!, checkFileName)
                    checkFile.mkdir()
                    //保存实时数据
                    FileUtils.copyFile(tempFile, File(checkFile, ConstantStr.CHECK_REAL_DATA))
                    //保存设置
                    val settingStr = Gson().toJson(checkType.settingBean)
                    FileUtils.writeStr2File(
                        settingStr,
                        File(checkFile, ConstantStr.CHECK_FILE_SETTING)
                    )
                    //保存遥测数据
                    FileUtils.writeByteArray2File(
                        ycData,
                        File(checkFile, ConstantStr.CHECK_YC_FILE_NAME)
                    )
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

    override fun toSaveData2File(source: ByteArray) {
        emitter?.onNext(source)
    }

    override fun isSaveData(): MutableLiveData<Boolean> {
        return isSaving
    }

    override fun openCheckFile() {
        val realFile = File(checkFile,ConstantStr.CHECK_REAL_DATA)
        if (realFile.exists()) {
            val fis = FileInputStream(realFile)
            var size = 0
            val buf = ByteArray(1024*4)
            while (fis.read(buf).also { size = it } != -1) {
                try {

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

    }

}