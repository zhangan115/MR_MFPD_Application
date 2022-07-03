package com.mr.mf_pd.application.view.check.continuity

import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.ProgressBar
import androidx.fragment.app.viewModels
import com.afollestad.materialdialogs.utils.MDUtil.getStringArray
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.databinding.ContinuityDataBinding
import com.mr.mf_pd.application.model.SettingBean
import com.mr.mf_pd.application.utils.LineChartUtils
import com.mr.mf_pd.application.view.base.BaseCheckFragment
import com.mr.mf_pd.application.view.base.ext.getViewModelFactory
import kotlinx.android.synthetic.main.fragment_continuity.*
import java.text.DecimalFormat
import kotlin.math.max
import kotlin.math.min

/**
 * 连续模式
 */
class ContinuityModelFragment : BaseCheckFragment<ContinuityDataBinding>() {

    override var TAG = "ContinuityModelFragment"
    private var defaultValues = listOf(20, 20, 5, 5)
    private var continuityMaxValue1: Int = 20
    private var continuityMaxValue2: Int = 20
    private var continuityMaxValue3: Int = 5
    private var continuityMaxValue4: Int = 5

    @Volatile
    var fzValue: Float? = null

    @Volatile
    var yxValue: Float? = null

    @Volatile
    var f1Hz: Float? = null

    @Volatile
    var f2Hz: Float? = null

    private val viewModel by viewModels<ContinuityModelViewModel> { getViewModelFactory() }

    companion object {

        fun create(isFile: Boolean): ContinuityModelFragment {
            val fragment = ContinuityModelFragment()
            val bundle = Bundle()
            bundle.putBoolean(ConstantStr.KEY_BUNDLE_BOOLEAN, isFile)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun lazyLoad() {
        viewModel.start()
    }

    override fun createCheckFile() {
        viewModel.createACheckFile()
    }

    override fun setCheckFile(str: String) {
        viewModel.setCheckFile(str)
    }

    override fun getContentView(): Int {
        return R.layout.fragment_continuity
    }

    override fun initData() {
        checkType = viewModel.checkType
        ycStateList =
            if (checkType == CheckType.AE || checkType == CheckType.AA || checkType == CheckType.TEV) {
                context?.getStringArray(R.array.aa_state_list)
            } else {
                context?.getStringArray(R.array.hf_state_list)
            }
        viewModel.toResetEvent.observe(this) {
            cleanCurrentData()
        }
    }

    override fun setIsFileValue(isFile: Boolean?) {
        viewModel.isFile.value = isFile
    }

    override fun initView() {
        LineChartUtils.initNoAxisChart(lineChart1)
        LineChartUtils.initNoAxisChart(lineChart2)
        LineChartUtils.initNoAxisChart(lineChart3)
        LineChartUtils.initNoAxisChart(lineChart4)
        viewModel.isSaveData?.observe(this, {
            if (it) {
                val animation =
                    AnimationUtils.loadAnimation(requireContext(), R.anim.twinkle_anim)
                image1.startAnimation(animation)
            } else {
                image1.clearAnimation()
            }
        })
        image1.setOnClickListener {
            if (image1.animation == null) {
                viewModel.startSaveData()
            } else {
                viewModel.stopSaveData()
                if (viewModel.filesRepository.getCurrentCheckName() != null) {
                    location = viewModel.filesRepository.getCurrentCheckName()
                }
                showSaveFileDialog()
            }
        }
        image2.setOnClickListener {
            checkActionListener?.downLimitValue()
        }
        image3.setOnClickListener {
            checkActionListener?.addLimitValue()
        }
        image4.setOnClickListener {
            cleanCurrentData()
        }
        viewModel.yxMinValue.postValue(minValue.toInt().toString())
        viewModel.fzMinValue.postValue(minValue.toInt().toString())
        viewModel.f1MinValue.postValue(minValue.toInt().toString())
        viewModel.f2MinValue.postValue(minValue.toInt().toString())
    }

    var maxValue: Float? = null
    var minValue: Float = 0f

    override fun onYcDataChange(bytes: ByteArray) {
        val valueList = splitBytesToValue(bytes)
        if (valueList.size >= 4) {
            view?.let {
                ycStateList?.let {
                    val state = valueList[0].toInt()
                    viewModel.setState(it[state])
                }
                var fzValue = valueList[2]
                var yxValue = valueList[3]
                var f1Hz = valueList[4]
                var f2Hz = valueList[5]
                val settingBean = viewModel.checkType.settingBean
                maxValue = settingBean.maxValue.toFloat()
                minValue = settingBean.minValue.toFloat()

                if (settingBean.gdCd == 0) {
                    valueList.forEach {
                        if (maxValue!! < it) {
                            maxValue = it
                        }
                    }
                } else {
                    fzValue = min(maxValue!!, fzValue)
                    fzValue = max(minValue, fzValue)

                    yxValue = min(maxValue!!, yxValue)
                    yxValue = max(minValue, yxValue)

                    f1Hz = min(maxValue!!, f1Hz)
                    f1Hz = max(minValue, f1Hz)

                    f2Hz = min(maxValue!!, f2Hz)
                    f2Hz = max(minValue, f2Hz)
                }
                this.fzValue = fzValue
                this.yxValue = yxValue
                this.f1Hz = f1Hz
                this.f2Hz = f2Hz

                viewModel.fzValueList.add(fzValue - minValue)
                viewModel.yxValueList.add(yxValue - minValue)
                viewModel.f1ValueList.add(f1Hz - minValue)
                viewModel.f2ValueList.add(f2Hz - minValue)


                continuityMaxValue1 =
                    calculationProgress(progressBar1, yxValue,defaultValues[0])
                continuityMaxValue2 =
                    calculationProgress(progressBar2, fzValue,defaultValues[1])
                continuityMaxValue3 = calculationProgress(progressBar3, f1Hz,defaultValues[2])
                continuityMaxValue4 = calculationProgress(progressBar4, f2Hz,defaultValues[3])

                viewModel.yxMaxValue.postValue(continuityMaxValue1.toString())
                viewModel.fzMaxValue.postValue(continuityMaxValue2.toString())
                viewModel.f1MaxValue.postValue(continuityMaxValue3.toString())
                viewModel.f2MaxValue.postValue(continuityMaxValue4.toString())

                if (viewModel.fzValueList.size > viewModel.checkType.settingBean.ljTime) {
                    viewModel.fzValueList.removeFirst()
                }
                if (viewModel.yxValueList.size > viewModel.checkType.settingBean.ljTime) {
                    viewModel.yxValueList.removeFirst()
                }
                if (viewModel.f1ValueList.size > viewModel.checkType.settingBean.ljTime) {
                    viewModel.f1ValueList.removeFirst()
                }
                if (viewModel.f2ValueList.size > viewModel.checkType.settingBean.ljTime) {
                    viewModel.f2ValueList.removeFirst()
                }

                LineChartUtils.updateData(lineChart1, viewModel.yxValueList)
                LineChartUtils.updateData(lineChart2, viewModel.fzValueList)
                LineChartUtils.updateData(lineChart3, viewModel.f1ValueList)
                LineChartUtils.updateData(lineChart4, viewModel.f2ValueList)
            }
        }
    }

    override fun onLimitValueChange(value: Int) {
        viewModel.limitValueStr.postValue("通道门限值:$value")
    }

    override fun cleanCurrentData() {
        viewModel.cleanCurrentData()
        LineChartUtils.updateData(lineChart1, viewModel.yxValueList)
        LineChartUtils.updateData(lineChart2, viewModel.fzValueList)
        LineChartUtils.updateData(lineChart3, viewModel.f1ValueList)
        LineChartUtils.updateData(lineChart4, viewModel.f2ValueList)
    }

    override fun isAdd(): Boolean {
        return isAdded
    }

    private fun calculationProgress(
        progressBar: ProgressBar,
        value: Float,
        defaultValue:Int
    ): Int {
        val mV = when {
            value < 100 -> {
                ((value + 10f) / 10).toInt() * 10
            }
            value > 100 -> {
                ((value + 100) / 100).toInt() * 100
            }
            else -> {
                defaultValue
            }
        }
        val m = max(mV, defaultValue)
        val progress = ((value - minValue) / (m - minValue) * 100).toInt()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            progressBar.setProgress(progress, true)
        } else {
            progressBar.progress = progress
        }
        return m
    }

    override fun setViewModel(dataBinding: ContinuityDataBinding?) {
        dataBinding?.vm = viewModel
    }

    override fun isSaving(): Boolean {
        return if (viewModel.isSaveData == null) false else viewModel.isSaveData!!.value!!
    }

    override fun cancelSaveData() {
        viewModel.isSaveData?.postValue(false)
    }

    override fun onResume() {
        super.onResume()
        cleanCurrentData()
    }

    override fun updateSettingBean(settingBean: SettingBean) {
        viewModel.checkType.settingBean = settingBean
        viewModel.updateTitle(settingBean)
    }

    override fun oneSecondUIChange() {
        val df1 = DecimalFormat("0.0")
        fzValue?.let {
            viewModel.fzValue.postValue(df1.format(it))
        }
        yxValue?.let {
            viewModel.yxValue.postValue(df1.format(it))
        }
        f1Hz?.let {
            viewModel.f1Value.postValue(df1.format(it))
        }
        f2Hz?.let {
            viewModel.f2Value.postValue(df1.format(it))
        }
    }

}