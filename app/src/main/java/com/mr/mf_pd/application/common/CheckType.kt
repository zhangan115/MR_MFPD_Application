package com.mr.mf_pd.application.common

import androidx.lifecycle.MutableLiveData
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.model.CheckParamsBean
import com.mr.mf_pd.application.model.SettingBean
import java.io.File

enum class CheckType(
    val type: Int,
    val description: Int,
    val checkName: Int,
    val checkFile: String,
    val icon: Int,
    val minValue: Float,
    val maxValue: Float,
    var settingBean:SettingBean,
    val checkParams: MutableLiveData<CheckParamsBean>
) {
    UHF(
        0,
        R.string.file_type_uhf,
        R.string.type_uhf,
        ".check_uhf",
        R.mipmap.img_check_icon,
        -85f, -10f,
        SettingBean(cacheKey = ConstantStr.SETTING_UHF),
        MutableLiveData(CheckParamsBean(0))
    ),
    AC(
        1,
        R.string.file_type_ac,
        R.string.type_ac,
        ".check_ac",
        R.mipmap.img_check_icon,
        0f, 100f,
        SettingBean(cacheKey = ConstantStr.SETTING_AC),
        MutableLiveData(CheckParamsBean(1))
    ),
    TEV(
        2,
        R.string.file_type_tev,
        R.string.type_tev,
        ".check_tev",
        R.mipmap.img_check_icon,
        -40f, 50f,
        SettingBean(cacheKey = ConstantStr.SETTING_TEV),
        MutableLiveData(CheckParamsBean(2))
    ),
    HF(
        3,
        R.string.file_type_hf,
        R.string.type_hf,
        ".check_hf",
        R.mipmap.img_check_icon,
        0f, 1900f,
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