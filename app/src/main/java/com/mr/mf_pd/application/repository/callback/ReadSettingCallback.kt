package com.mr.mf_pd.application.repository.callback

import com.mr.mf_pd.application.model.FileBeanModel

interface ReadSettingCallback {

    fun onGetFileBean(fileBean: FileBeanModel)

}