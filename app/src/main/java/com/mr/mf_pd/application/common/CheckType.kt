package com.mr.mf_pd.application.common

import androidx.lifecycle.MutableLiveData
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.model.CheckParamsBean
import com.mr.mf_pd.application.model.SettingBean

enum class CheckType(
    val passageway: Int,//通道号
    val description: Int,//描述
    val checkName: Int,//检测名称
    val checkFile: String,//检测文件类型
    val icon: Int,//图标
    var settingBean: SettingBean,//设置
    var settingLength: Int,//设置数据长度
    var commandType:Int,//命令码
    var defaultUnit:String,//默认单位
    var cacheKey:String,//设置保存的数据Key
    val checkParams: MutableLiveData<CheckParamsBean>,//检测数据
) {
    UHF(
        0,
        R.string.file_type_uhf,
        R.string.type_uhf,
        "uhf",
        R.mipmap.img_check_icon,
        SettingBean(),
        11,
        1,
        "dBm",
        ConstantStr.SETTING_UHF,
        MutableLiveData(CheckParamsBean(0))
    ),
    TEV(
        1,
        R.string.file_type_tev,
        R.string.type_tev,
        "tev",
        R.mipmap.img_check_icon,
        SettingBean(),
        10,
        1,
        "dBmV",
        ConstantStr.SETTING_TEV,
        MutableLiveData(CheckParamsBean(1))
    ),
    AE(
        2,
        R.string.file_type_ac,
        R.string.type_ac,
        "ae",
        R.mipmap.img_check_icon,
        SettingBean(),
        13,
        3,
        "mV",
        ConstantStr.SETTING_AE,
        MutableLiveData(CheckParamsBean(2))
    ),
    AA(
        4,
        R.string.file_type_aa,
        R.string.type_ac,
        "aa",
        R.mipmap.img_check_icon,
        SettingBean(),
        13,
        3,
        "mV",
        ConstantStr.SETTING_AA,
        MutableLiveData(CheckParamsBean(4))
    ),
    HF(
        3,
        R.string.file_type_hf,
        R.string.type_hf,
        "hf",
        R.mipmap.img_check_icon,
        SettingBean(),
        13,
        1,
        "mV",
        ConstantStr.SETTING_HF,
        MutableLiveData(CheckParamsBean(3))
    );
}