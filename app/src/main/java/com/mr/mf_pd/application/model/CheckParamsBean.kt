package com.mr.mf_pd.application.model

data class CheckParamsBean(var type: Int) {

    var fzAttr: String? = null//幅值
    var phaseAttr: String = "内同步"//内同步
    var hzAttr: String? = null//频率
    var mcCountAttr: String? = null//脉冲
    var frequencyBandAttr: String = "全通"//检测频带

    var effectiveValueAttr: String? = null//有效值
    var bandThresholdAttr: String? = null//

}