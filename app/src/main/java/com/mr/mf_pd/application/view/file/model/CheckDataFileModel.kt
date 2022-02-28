package com.mr.mf_pd.application.view.file.model

import com.mr.mf_pd.application.utils.FileTypeUtils
import java.io.File

 class CheckDataFileModel {
    var isCheckFile = false //是否是检测文件
    var file //当前的检测文件
            : File? = null
    var marks //文件描述
            : String? = null
    var isHasPhoto //是否存在图片
            = false
    var color = -1 //文件标识颜色
    var fileType //检测文件类型
            : FileTypeUtils.FileType? = null
    var isSelect //是否被选中
            = false
    var isToChooseModel = false
}