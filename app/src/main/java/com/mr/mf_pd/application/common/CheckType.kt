package com.mr.mf_pd.application.common

import androidx.lifecycle.MutableLiveData
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.model.CheckParamsBean
import com.mr.mf_pd.application.model.SettingBean
import java.io.File

enum class CheckType(
    val type: Int,//通道号
    val description: Int,//描述
    val checkName: Int,//检测名称
    val checkFile: String,//检测文件类型
    val icon: Int,//图标
    var settingBean: SettingBean,//设置
    val checkParams: MutableLiveData<CheckParamsBean>,//检测数据
) {
    UHF(
        0,
        R.string.file_type_uhf,
        R.string.type_uhf,
        ".check_uhf",
        R.mipmap.img_check_icon,
        SettingBean(cacheKey = ConstantStr.SETTING_UHF),
        MutableLiveData(CheckParamsBean(0))
    ),
    TEV(
        1,
        R.string.file_type_tev,
        R.string.type_tev,
        ".check_tev",
        R.mipmap.img_check_icon,
        SettingBean(cacheKey = ConstantStr.SETTING_TEV),
        MutableLiveData(CheckParamsBean(2))
    ),
    AE(
        2,
        R.string.file_type_ac,
        R.string.type_ac,
        ".check_ac",
        R.mipmap.img_check_icon,
        SettingBean(cacheKey = ConstantStr.SETTING_AC),
        MutableLiveData(CheckParamsBean(1))
    ),
    HF(
        3,
        R.string.file_type_hf,
        R.string.type_hf,
        ".check_hf",
        R.mipmap.img_check_icon,
        SettingBean(cacheKey = ConstantStr.SETTING_HF),
        MutableLiveData(CheckParamsBean(3))
    );

    /**
     * 获取文件类型获取检测类型
     * @param file 文件
     */
    fun getCheckTypeByFile(file: File): CheckType? {
        if (file.startsWith(checkFile)) {
            return this
        }
        return null
    }

    /**
     * 通过Index获取检测类型
     * @param index index
     */
    fun getCheckTypeByIndex(index: Int): CheckType? {
        if (index == type) {
            return this
        }
        return null
    }
}