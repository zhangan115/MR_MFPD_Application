package com.mr.mf_pd.application.view.check.continuity

import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.ProgressBar
import androidx.fragment.app.viewModels
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.databinding.ContinuityDataBinding
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
    }

    var maxValue: Float? = null
    var minValue: Float? = null

    override fun onYcDataChange(bytes: ByteArray) {
        val valueList = splitBytesToValue(bytes)
        if (valueList.size >= 4) {
            view?.let {
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
                        if (minValue!! > it) {
                            minValue = it
                        }
                    }
                } else {
                    fzValue = min(maxValue!!, fzValue)
                    fzValue = max(minValue!!, fzValue)

                    yxValue = min(maxValue!!, yxValue)
                    yxValue = max(minValue!!, yxValue)

                    f1Hz = min(maxValue!!, f1Hz)
                    f1Hz = max(minValue!!, f1Hz)

                    f2Hz = min(maxValue!!, f2Hz)
                    f2Hz = max(minValue!!, f2Hz)
                }

                val df1 = DecimalFormat("0.00")

                viewModel.fzValueList.add(fzValue - minValue!!)
                viewModel.fzValue.postValue(df1.format(fzValue))

                viewModel.yxValueList.add(yxValue - minValue!!)
                viewModel.yxValue.postValue(df1.format(yxValue))

                viewModel.f1ValueList.add(f1Hz - minValue!!)
                viewModel.f1Value.postValue(df1.format(f1Hz))

                viewModel.f2ValueList.add(f2Hz - minValue!!)
                viewModel.f2Value.postValue(df1.format(f2Hz))

                calculationProgress(progressBar1, yxValue)
                calculationProgress(progressBar2, fzValue)
                calculationProgress(progressBar3, f1Hz)
                calculationProgress(progressBar4, f2Hz)

                viewModel.yxMinValue.postValue(minValue?.toString())
                viewModel.fzMinValue.postValue(minValue?.toString())
                viewModel.f1MinValue.postValue(minValue?.toString())
                viewModel.f2MinValue.postValue(minValue?.toString())

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

    private fun calculationProgress(progressBar: ProgressBar, value: Float) {
        val progress = ((value - minValue!!) / (maxValue!! - minValue!!) * 100).toInt()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            progressBar.setProgress(progress, true)
        } else {
            progressBar.progress = progress
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

}