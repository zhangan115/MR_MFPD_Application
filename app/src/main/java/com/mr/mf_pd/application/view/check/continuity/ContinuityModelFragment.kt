package com.mr.mf_pd.application.view.check.continuity

import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.ProgressBar
import androidx.fragment.app.viewModels
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.databinding.ContinuityDataBinding
import com.mr.mf_pd.application.model.SettingBean
import com.mr.mf_pd.application.utils.LineChartUtils
import com.mr.mf_pd.application.utils.ZLog
import com.mr.mf_pd.application.view.base.BaseCheckFragment
import com.mr.mf_pd.application.view.base.ext.getViewModelFactory
import kotlinx.android.synthetic.main.fragment_continuity.*
import java.text.DecimalFormat
import kotlin.math.max

/**
 * 连续模式
 */
class ContinuityModelFragment : BaseCheckFragment<ContinuityDataBinding>() {

    override var TAG = "ContinuityModelFragment"
    private var defaultValues = listOf(5.0f, 5.0f, 1.0f, 1.0f)
    private var continuityMaxValue1: Float = 5.0f
    private var continuityMaxValue2: Float = 5.0f
    private var continuityMaxValue3: Float = 1.0f
    private var continuityMaxValue4: Float = 1.0f

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
        locationLayout.setOnClickListener {
            createChooseFileIntent()
        }
    }

    var maxValue: Float? = null

    override fun onYcDataChange(bytes: ByteArray) {
        val valueList = splitBytesToValue(bytes)
        if (valueList.size >= 4) {
            view?.let {
                val fzValue = valueList[2]
                val yxValue = valueList[3]
                val f1Hz = valueList[4]
                val f2Hz = valueList[5]
                val settingBean = viewModel.checkType.settingBean
                maxValue = settingBean.maxValue.toFloat()

                valueList.forEach {
                    if (maxValue!! < it) {
                        maxValue = it
                    }
                }
                this.fzValue = fzValue
                this.yxValue = yxValue
                this.f1Hz = f1Hz
                this.f2Hz = f2Hz

                viewModel.fzValueList.add(fzValue)
                viewModel.yxValueList.add(yxValue)
                viewModel.f1ValueList.add(f1Hz)
                viewModel.f2ValueList.add(f2Hz)


                if (viewModel.fzValueList.size > viewModel.checkType.settingBean.ljTime * 10) {
                    viewModel.fzValueList.removeFirst()
                }
                if (viewModel.yxValueList.size > viewModel.checkType.settingBean.ljTime * 10) {
                    viewModel.yxValueList.removeFirst()
                }
                if (viewModel.f1ValueList.size > viewModel.checkType.settingBean.ljTime * 10) {
                    viewModel.f1ValueList.removeFirst()
                }
                if (viewModel.f2ValueList.size > viewModel.checkType.settingBean.ljTime * 10) {
                    viewModel.f2ValueList.removeFirst()
                }

                continuityMaxValue2 =
                    calculationProgress(progressBar2, fzValue, defaultValues[1])
                continuityMaxValue3 = calculationProgress(progressBar3, f1Hz, defaultValues[2])
                continuityMaxValue4 = calculationProgress(progressBar4, f2Hz, defaultValues[3])

                continuityMaxValue1 = if (viewModel.checkType == CheckType.AA || viewModel.checkType == CheckType.AE) {
                    calculationProgress(progressBar1,
                        yxValue,
                        defaultValues[0],
                        continuityMaxValue2 / 2)
                } else {
                    calculationProgress(progressBar1, yxValue, defaultValues[0])
                }
                val df1 = DecimalFormat("0.0")
                viewModel.yxMaxValue.postValue(df1.format(continuityMaxValue1))
                viewModel.fzMaxValue.postValue(df1.format(continuityMaxValue2))
                viewModel.f1MaxValue.postValue(df1.format(continuityMaxValue3))
                viewModel.f2MaxValue.postValue(df1.format(continuityMaxValue4))


                LineChartUtils.updateData(lineChart1,
                    viewModel.yxValueList,
                    0f,
                    calculationMaxValue(continuityMaxValue1, viewModel.yxValueList))
                LineChartUtils.updateData(lineChart2,
                    viewModel.fzValueList,
                    0f,
                    calculationMaxValue(continuityMaxValue2, viewModel.fzValueList))
                LineChartUtils.updateData(lineChart3,
                    viewModel.f1ValueList,
                    0f,
                    calculationMaxValue(continuityMaxValue3, viewModel.f1ValueList))
                LineChartUtils.updateData(lineChart4,
                    viewModel.f2ValueList,
                    0f,
                    calculationMaxValue(continuityMaxValue4, viewModel.f2ValueList))
            }
        }
    }

    override fun fdStrChange(fdType: String?) {
        viewModel.setState(fdType)
    }

    override fun onLimitValueChange(value: Int) {
        viewModel.limitValueStr.postValue("通道门限值:$value")
    }

    override fun cleanCurrentData() {
        viewModel.cleanCurrentData()
        LineChartUtils.updateData(lineChart1,
            viewModel.yxValueList,
            0f,
            continuityMaxValue1)
        LineChartUtils.updateData(lineChart2,
            viewModel.fzValueList,
            0f,
            continuityMaxValue2)
        LineChartUtils.updateData(lineChart3,
            viewModel.f1ValueList,
            0f,
            continuityMaxValue3)
        LineChartUtils.updateData(lineChart4,
            viewModel.f2ValueList,
            0f,
            continuityMaxValue4)
    }

    override fun isAdd(): Boolean {
        return isAdded
    }

    private fun calculationMaxValue(currentMaxValue: Float, values: ArrayList<Float>): Float {
        values.maxOrNull()?.let {
            if (it < currentMaxValue) {
                return it
            }
        }
        return currentMaxValue
    }

    private fun calculationProgress(
        progressBar: ProgressBar,
        value: Float,
        defaultValue: Float,
        maxValue: Float? = null,
    ): Float {
        ZLog.d(TAG, "progress is = " + progressBar + "value = " + value, showLog = false)
        val m: Float = maxValue ?: max(getMaxValue(value, defaultValue), defaultValue)
        val progress = ((value) / (m) * 100).toInt()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            progressBar.setProgress(progress, true)
        } else {
            progressBar.progress = progress
        }
        return m
    }

    private fun getMaxValue(value: Float, defaultValue: Float): Float {
        return when {
            value < 10 -> {
                (value + 1f).toInt().toFloat()
            }
            value < 100 && value >= 10 -> {
                (((value + 10f) / 10).toInt() * 10).toFloat()
            }
            value > 100 -> {
                (((value + 100) / 100).toInt() * 100).toFloat()
            }
            else -> {
                defaultValue
            }
        }
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