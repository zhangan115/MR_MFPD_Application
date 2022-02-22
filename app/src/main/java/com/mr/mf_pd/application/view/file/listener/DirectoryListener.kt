package com.mr.mf_pd.application.view.file.listener

import com.mr.mf_pd.application.utils.FileTypeUtils

interface DirectoryListener {
    fun onFileTypeChange(fileType: FileTypeUtils.FileType)
}