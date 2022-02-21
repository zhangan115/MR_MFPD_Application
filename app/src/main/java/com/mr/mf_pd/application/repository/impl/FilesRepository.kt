package com.mr.mf_pd.application.repository.impl

import androidx.lifecycle.MutableLiveData
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.utils.FileTypeUtils
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

    /**
     * 设置当前文件地址
     * @param file 文件
     */
    fun setCurrentChickFile(file: File)

    /**
     * 生成一条监测数据
     */
    fun toCreateCheckFile(checkType: CheckType, ycData: ByteArray?)

    /**
     * 当前项目地址
     * @return 文件
     */
    fun getCurrentCheckFile(): File?

    /**
     * 获取当前的项目地址名称
     * @return 名称
     */
    fun getCurrentCheckName(): String?

    /**
     * 保存数据到文件中
     */
    fun toSaveData2File(source: ByteArray)

    /**
     * 数据保存状态
     */
    fun isSaveData(): MutableLiveData<Boolean>?

}