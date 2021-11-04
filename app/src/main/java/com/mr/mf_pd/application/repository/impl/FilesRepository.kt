package com.mr.mf_pd.application.repository.impl

import java.io.File

interface FilesRepository {

    /**
     * 开始保存数据
     */
    fun startSaveData()

    /**
     * 停止保存
     */
    fun stopSaveData()

    fun getCurrentCheckFile(): File//当前项目地址

    fun toSaveData2File()//保存数据到文件中


}