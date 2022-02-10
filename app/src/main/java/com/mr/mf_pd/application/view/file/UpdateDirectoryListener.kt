package com.mr.mf_pd.application.view.file

import com.mr.mf_pd.application.view.file.model.CheckDataFileModel

interface UpdateDirectoryListener {

    fun updateDirectory(action: FilePickerActivity.ActionType)

    fun getSelectData(): List<CheckDataFileModel>

    fun updateFiles()
}