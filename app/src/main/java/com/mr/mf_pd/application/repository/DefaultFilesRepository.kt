package com.mr.mf_pd.application.repository

import androidx.lifecycle.MutableLiveData
import com.mr.mf_pd.application.repository.impl.FilesRepository
import java.io.File

class DefaultFilesRepository : FilesRepository {

    var isSaving: MutableLiveData<Boolean>? = null

    init {
        isSaving = MutableLiveData(false)
    }

    override fun startSaveData() {
        isSaving?.postValue(true)
    }

    override fun stopSaveData() {
        isSaving?.postValue(false)
    }

    override fun getCurrentCheckFile(): File {
        return File("")
    }

    override fun toSaveData2File() {

    }

    override fun isSaveData(): MutableLiveData<Boolean>? {
        return isSaving
    }

}