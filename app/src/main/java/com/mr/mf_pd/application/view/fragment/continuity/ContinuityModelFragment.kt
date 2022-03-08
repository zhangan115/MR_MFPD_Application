package com.mr.mf_pd.application.view.fragment.continuity

import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.fragment.app.viewModels
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.databinding.ContinuityDataBinding
import com.mr.mf_pd.application.model.DeviceBean
import com.mr.mf_pd.application.utils.LineChartUtils
import com.mr.mf_pd.application.view.base.BaseCheckFragment

import com.mr.mf_pd.application.view.base.BaseFragment
import com.mr.mf_pd.application.view.base.ext.getViewModelFactory
import kotlinx.android.synthetic.main.fragment_continuity.*
import kotlinx.android.synthetic.main.fragment_continuity.image1
import kotlinx.android.synthetic.main.fragment_continuity.image2
import kotlinx.android.synthetic.main.fragment_continuity.image3
import kotlinx.android.synthetic.main.fragment_continuity.image4
import kotlinx.android.synthetic.main.fragment_phase.*
import java.text.DecimalFormat

/**
 * 连续模式
 */
class ContinuityModelFragment : BaseCheckFragment<ContinuityDataBinding>() {

    private val viewModel by viewModels<ContinuityModelViewModel> { getViewModelFactory() }

    companion object {

        fun create(): ContinuityModelFragment {
            val fragment = ContinuityModelFragment()
            val bundle = Bundle()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun lazyLoad() {
        viewModel.start()
    }


    override fun setCheckFile(str: String) {
        viewModel.setCheckFile(str)
    }

    override fun getContentView(): Int {
        return R.layout.fragment_continuity
    }

    override fun initData() {

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
            viewModel.cleanCurrentData()
        }
    }

    override fun onYcDataChange(bytes: ByteArray) {
        val valueList = splitBytesToValue(bytes)
        if (valueList.size >= 4) {
            view?.let {
                val fzValue = valueList[2]
                viewModel.fzValueList.add(fzValue)
                val df1 = DecimalFormat("0.00")
                viewModel.fzValue.postValue(df1.format(fzValue))
                val yxValue = valueList[3]
                viewModel.yxValueList.add(yxValue)
                viewModel.yxValue.postValue(df1.format(yxValue))
                val f1Hz = valueList[4]
                viewModel.f1ValueList.add(f1Hz)
                viewModel.f1Value.postValue(df1.format(f1Hz))
                val f2Hz = valueList[5]
                viewModel.f2ValueList.add(f2Hz)
                viewModel.f2Value.postValue(df1.format(f2Hz))

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
            }
        }
    }

    override fun setViewModel(dataBinding: ContinuityDataBinding?) {
        dataBinding?.vm = viewModel
    }

    override fun isSaving(): Boolean {
        return false
    }

    override fun cancelSaveData() {
        viewModel.isSaveData?.postValue(false)
    }


}