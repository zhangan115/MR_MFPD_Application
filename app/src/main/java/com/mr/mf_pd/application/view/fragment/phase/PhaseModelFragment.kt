package com.mr.mf_pd.application.view.fragment.phase

import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.fragment.app.viewModels
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.databinding.PhaseDataBinding
import com.mr.mf_pd.application.model.DeviceBean
import com.mr.mf_pd.application.repository.DefaultDataRepository
import com.mr.mf_pd.application.view.base.BaseCheckFragment
import com.mr.mf_pd.application.view.base.ext.getViewModelFactory
import com.mr.mf_pd.application.view.renderer.PointChartsRenderer
import com.mr.mf_pd.application.view.renderer.PrPsChartsRenderer
import kotlinx.android.synthetic.main.fragment_phase.*
import java.text.DecimalFormat

class PhaseModelFragment : BaseCheckFragment<PhaseDataBinding>() {

    private val viewModel by viewModels<PhaseModelViewModel> { getViewModelFactory() }
    private var rendererSet = false
    var pointChartsRenderer: PointChartsRenderer? = null

    companion object {

        fun create(): PhaseModelFragment {
            val fragment = PhaseModelFragment()
            val bundle = Bundle()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun lazyLoad() {
        viewModel.start()
    }

    override fun getContentView(): Int {
        return R.layout.fragment_phase
    }

    override fun initData() {

    }

    override fun initView() {
        surfaceView1.setEGLContextClientVersion(3)
        pointChartsRenderer = PointChartsRenderer(this.requireContext(), getYAxisValue())
        surfaceView1.setRenderer(pointChartsRenderer)
        pointChartsRenderer?.getPrpsValueCallback =
            object : PrPsChartsRenderer.GetPrpsValueCallback {
                override fun getData() {
                    pointChartsRenderer?.updateYAxis(getYAxisValue())
                    viewModel.getPhaseData().forEach {
                        pointChartsRenderer?.addPrpsData(it)
                    }
                }
            }
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
            pointChartsRenderer?.cleanData()
        }
        image5.setOnClickListener {
            checkActionListener?.changeBandDetectionModel()
        }
        if (viewModel.checkType == CheckType.HF || viewModel.checkType == CheckType.TEV) {
            image5.visibility = View.GONE
        }
        rendererSet = true
    }

    override fun setCheckFile(str: String) {
        viewModel.setCheckFile(str)
    }

    override fun createCheckFile() {
        viewModel.createACheckFile()
    }

    private fun getYAxisValue(): ArrayList<String> {
        val yList = ArrayList<String>()
        if (viewModel.checkType.settingBean.gdCd == 1) {
            val value =
                viewModel.checkType.settingBean.maxValue - viewModel.checkType.settingBean.minValue
            val step = value / 4
            for (i in 0..4) {
                val str = viewModel.checkType.settingBean.minValue + step * i
                yList.add(str.toString())
            }
        } else {
            if (DefaultDataRepository.realDataMaxValue.value != null && DefaultDataRepository.realDataMinValue.value != null) {
                val value =
                    DefaultDataRepository.realDataMaxValue.value!! - DefaultDataRepository.realDataMinValue.value!!
                val step = value / 4
                for (i in 0..4) {
                    val str = DefaultDataRepository.realDataMinValue.value!! + step * i
                    yList.add(str.toString())
                }
            }
        }
        yList.reverse()
        return yList
    }

    override fun setViewModel(dataBinding: PhaseDataBinding?) {
        dataBinding?.vm = viewModel
    }

    override fun onResume() {
        super.onResume()
        if (rendererSet) {
            surfaceView1.onResume()
        }
        viewModel.cleanCurrentData()
        pointChartsRenderer?.cleanData()
    }

    override fun onPause() {
        super.onPause()
        if (rendererSet) {
            surfaceView1.onPause()
        }
    }

    override fun onYcDataChange(bytes: ByteArray) {
        val valueList = splitBytesToValue(bytes)
        if (valueList.size >= 2) {
            view?.let {
                viewModel.checkType.checkParams.value?.hzAttr = valueList[1].toString()
                if ((viewModel.checkType == CheckType.TEV || viewModel.checkType == CheckType.AE) && valueList.size >= 6) {
                    val df1 = DecimalFormat("0.00")
                    viewModel.checkType.checkParams.value?.effectiveValueAttr = df1.format(valueList[3])
                }
                viewModel.checkType.checkParams.postValue(viewModel.checkType.checkParams.value)
            }
        }
    }

    override fun isSaving(): Boolean {
        return if (viewModel.isSaveData == null) false else viewModel.isSaveData!!.value!!
    }

    override fun cancelSaveData() {
        viewModel.isSaveData?.postValue(false)
    }
}