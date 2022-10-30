package com.mr.mf_pd.application.view.file.model

import com.mr.mf_pd.application.utils.FileTypeUtils
import java.io.File

class CheckDataFileModel {
    var isCheckFile = false //是否是检测文件
    var file: File? = null//当前的检测文件
    var marks: String? = null //文件描述
    var isHasPhoto = false//是否存在图片
    var color = -1 //文件标识颜色
    var fileType: FileTypeUtils.FileType? = null //检测文件类型
    var isSelect = false//是否被选中
    var isToChooseModel = false
    var dataTime: Long? = null//文件时间
    var checkDataList: List<CheckDataFileModel> = ArrayList()//文件夹下面的检测数据
}