package com.mr.mf_pd.application.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.mr.mf_pd.application.app.MRApplication
import com.mr.mf_pd.application.repository.impl.FilesRepository
import com.mr.mf_pd.application.utils.DateUtil
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.Observables
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
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
        val tempDir = File(MRApplication.instance.fileCacheFile(), "temp")
        if (!tempDir.exists()) {
            tempDir.mkdir()
        }
        tempFile = File(tempDir, fileName)
        if (!tempFile!!.exists()) {
            tempFile!!.createNewFile()
        }
        fileOutputStream = FileOutputStream(tempFile, true)
        val obs = ObservableOnSubscribe<ByteArray> {
            emitter = it
        }
        Observable.create(obs).doOnNext {
            fileOutputStream?.write(it)
            fileOutputStream?.flush()
        }.observeOn(Schedulers.io())
            .subscribeOn(Schedulers.io())
            .doOnComplete {
                Log.d("zhangan",
                    tempFile?.absolutePath.toString() + " file size is " + tempFile?.length())
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

    override fun getCurrentCheckFile(): File? {
        if (checkFile != null) {
            return checkFile
        }
        return MRApplication.instance.fileCacheFile()
    }

    override fun getCurrentCheckName(): String? {
        checkFile?.let {
            return it.absolutePath.removeRange(0, MRApplication.instance.fileCacheFile()!!.absolutePath.length)
        }
        return null
    }

    override fun toSaveData2File(source: ByteArray) {
        emitter?.onNext(source)
    }

    override fun isSaveData(): MutableLiveData<Boolean> {
        return isSaving
    }

}