package com.mr.mf_pd.application.repository.impl

import androidx.lifecycle.MutableLiveData
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.manager.socket.callback.BaseDataCallback
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
    fun setCurrentClickFile(file: File)

    /**
     * 生成一条监测数据
     */
    fun toCreateCheckFile(checkType: CheckType)

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
    fun toSaveRealData2File(source: ByteArray)

    /**
     * 保存遥测数据到文件中
     */
    fun toSaveYCData2File(source: ByteArray)

    /**
     * 数据保存状态
     */
    fun isSaveData(): MutableLiveData<Boolean>?

    /**
     * 设置检测文件
     */
    fun openCheckFile(file:File)

    /**
     * 获取检测类型
     */
    fun getCheckType(): CheckType

    /**
     * 数据监测
     */
    fun addDataListener()

    /**
     * 增加遥测数据的回调
     */
    fun addYcDataCallback(callback: BaseDataCallback)

    /**
     * 移除遥测数据回调
     */
    fun removeYcDataCallback(callback: BaseDataCallback)

    /**
     * 获取线性图表数据
     */
    fun getGainValueList(): MutableLiveData<ArrayList<Float>>

    /**
     * 释放读取文件的操作
     */
    fun releaseReadFile()

}