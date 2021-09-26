package com.mr.mf_pd.application.repository

import com.mr.mf_pd.application.repository.impl.FilesRepository
import java.io.File

class DefaultFilesRepository:FilesRepository {

    override fun getCurrentCheckFile(): File {
        return File("")
    }

    override fun toSaveData2File() {

    }

}