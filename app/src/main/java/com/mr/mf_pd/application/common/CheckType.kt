package com.mr.mf_pd.application.common

import androidx.lifecycle.MutableLiveData
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.model.CheckParamsBean
import com.mr.mf_pd.application.model.SettingBean
import java.io.File

enum class CheckType(
    val passageway: Int,//通道号
    val description: Int,//描述
    val checkName: Int,//检测名称
    val checkFile: String,//检测文件类型
    val icon: Int,//图标
    var settingBean: SettingBean,//设置
    var settingLength: Int,//设置数据长度
    var commandType:Int,//命令码
    val checkParams: MutableLiveData<CheckParamsBean>,//检测数据
) {
    UHF(
        0,
        R.string.file_type_uhf,
        R.string.type_uhf,
        ".check_uhf",
        R.mipmap.img_check_icon,
        SettingBean(cacheKey = ConstantStr.SETTING_UHF),
        11,
        1,
        MutableLiveData(CheckParamsBean(0))
    ),
    TEV(
        1,
        R.string.file_type_tev,
        R.string.type_tev,
        ".check_tev",
        R.mipmap.img_check_icon,
        SettingBean(cacheKey = ConstantStr.SETTING_TEV),
        10,
        1,
        MutableLiveData(CheckParamsBean(1))
    ),
    AE(
        2,
        R.string.file_type_ac,
        R.string.type_ac,
        ".check_ae",
        R.mipmap.img_check_icon,
        SettingBean(cacheKey = ConstantStr.SETTING_AC),
        13,
        1,
        MutableLiveData(CheckParamsBean(2))
    ),
    HF(
        3,
        R.string.file_type_hf,
        R.string.type_hf,
        ".check_hf",
        R.mipmap.img_check_icon,
        SettingBean(cacheKey = ConstantStr.SETTING_HF),
        13,
        1,
        MutableLiveData(CheckParamsBean(3))
    );
}