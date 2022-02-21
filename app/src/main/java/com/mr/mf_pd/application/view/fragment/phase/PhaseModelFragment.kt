package com.mr.mf_pd.application.view.fragment.phase

import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.fragment.app.viewModels
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.databinding.PhaseDataBinding
import com.mr.mf_pd.application.model.DeviceBean
import com.mr.mf_pd.application.view.base.BaseCheckFragment
import com.mr.mf_pd.application.view.base.ext.getViewModelFactory
import com.mr.mf_pd.application.view.renderer.PointChartsRenderer
import com.mr.mf_pd.application.view.renderer.PrPsChartsRenderer
import kotlinx.android.synthetic.main.fragment_phase.*

class PhaseModelFragment : BaseCheckFragment<PhaseDataBinding>() {

    private val viewModel by viewModels<PhaseModelViewModel> { getViewModelFactory() }
    private var rendererSet = false
    var pointChartsRenderer: PointChartsRenderer? = null

    companion object {

        fun create(deviceBean: DeviceBean?): PhaseModelFragment {
            val fragment = PhaseModelFragment()
            val bundle = Bundle()
            bundle.putParcelable(ConstantStr.KEY_BUNDLE_OBJECT, deviceBean)
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
        rendererSet = true
    }

    override fun setCheckFile(str:String) {
        viewModel.ycByteArray = checkActionListener?.getYcByteArray()
        viewModel.setCheckFile(str)
    }

    override fun createCheckFile() {
        viewModel.createACheckFile()
    }

    private fun getYAxisValue(): ArrayList<String> {
        val value =
            viewModel.checkType.settingBean.maxValue - viewModel.checkType.settingBean.minValue
        val step = value / 4
        val yList = ArrayList<String>()
        for (i in 0..4) {
            val str = viewModel.checkType.settingBean.minValue + step * i
            yList.add(str.toString())
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
//        pointChartsRenderer?.updateYAxis(getYAxisValue())
        viewModel.cleanCurrentData()
        pointChartsRenderer?.cleanData()
    }

    override fun onPause() {
        super.onPause()
        if (rendererSet) {
            surfaceView1.onPause()
        }
    }

    override fun isSaving(): Boolean {
        return if (viewModel.isSaveData == null) false else viewModel.isSaveData!!.value!!
    }

    override fun cancelSaveData() {
        viewModel.isSaveData?.postValue(false)
    }
}