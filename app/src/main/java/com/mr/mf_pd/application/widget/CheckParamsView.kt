package com.mr.mf_pd.application.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.model.CheckParamsBean
import kotlinx.android.synthetic.main.layout_check_params.view.*

class CheckParamsView : LinearLayout {

    constructor(context: Context?) : super(context!!) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    ) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context!!, attrs, defStyle
    ) {
        init()
    }

    private fun init() {
        inflate(context, R.layout.layout_check_params, this)
    }

    fun setData(checkParamsBean: CheckParamsBean?) {
        fzTextValue.text = checkParamsBean?.fzAttr
        plText.text = checkParamsBean?.hzAttr
        tbStateText.text = checkParamsBean?.phaseAttr
        when (checkParamsBean?.passageway) {
            0 -> {
                tempText1.text = "脉冲数："
                tempText2.text = "检测频带："
                tempText1Value.text = checkParamsBean.mcCountAttr
                tempText2Value.text = checkParamsBean.frequencyBandAttr
                checkParams4Layout.visibility = GONE
            }
            1 -> {
                tempText1.text = "有效值："
                tempText2.text = "检测频带："
                tempText1Value.text = checkParamsBean.effectiveValueAttr
                tempText2Value.text = checkParamsBean.frequencyBandAttr
                checkParams4Layout.visibility = VISIBLE
                tempText4.text = "脉冲数："
                tempText4Value.text = checkParamsBean.mcCountAttr
            }
            2 -> {
                tempText1.text = "脉冲数："
                tempText2.text = "有效值："
                tempText1Value.text = checkParamsBean.mcCountAttr
                tempText2Value.text = checkParamsBean.effectiveValueAttr
                checkParams4Layout.visibility = GONE
            }
            3 -> {
                tempText1.text = "脉冲数："
                tempText2.text = ""
                tempText1Value.text = checkParamsBean.mcCountAttr
                tempText2Value.text = ""
                checkParams4Layout.visibility = GONE
            }
            4 -> {
                tempText1.text = "脉冲数："
                tempText2.text = ""
                tempText1Value.text = checkParamsBean.mcCountAttr
                tempText2Value.text = ""
                checkParams4Layout.visibility = GONE
            }
        }
    }
}