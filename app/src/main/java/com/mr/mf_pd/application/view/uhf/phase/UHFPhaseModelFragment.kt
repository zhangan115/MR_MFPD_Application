package com.mr.mf_pd.application.view.uhf.phase

import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.viewModels
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.databinding.UHFPhaseDataBinding
import com.mr.mf_pd.application.model.DeviceBean
import com.mr.mf_pd.application.view.base.BaseFragment
import com.mr.mf_pd.application.view.base.ext.getViewModelFactory
import com.mr.mf_pd.application.view.opengl.`object`.Point2DChartPoint
import com.mr.mf_pd.application.view.uhf.renderer.PointChartsRenderer
import com.mr.mf_pd.application.view.uhf.renderer.ValueChangeRenderer
import kotlinx.android.synthetic.main.fragment_uhf_phase.*

class UHFPhaseModelFragment : BaseFragment<UHFPhaseDataBinding>() {

    private val viewModel by viewModels<UHFPhaseModelViewModel> { getViewModelFactory() }
    private var rendererSet = false

    companion object {

        fun create(deviceBean: DeviceBean?): UHFPhaseModelFragment {
            val fragment = UHFPhaseModelFragment()
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
        return R.layout.fragment_uhf_phase
    }

    override fun initData() {

    }

    var pointChartsRenderer: PointChartsRenderer? = null
    var valueChangeRenderer: ValueChangeRenderer? = null

    override fun initView() {
        surfaceView1.setEGLContextClientVersion(3)
        surfaceView2.setEGLContextClientVersion(3)
        pointChartsRenderer = PointChartsRenderer(this.requireContext())
        valueChangeRenderer =
            ValueChangeRenderer(this.requireContext())

        surfaceView1.setRenderer(pointChartsRenderer)
        surfaceView2.setRenderer(valueChangeRenderer)

        image1.setOnClickListener {
            pointValueChange()
        }

        image2.setOnClickListener {
            valueChange()
        }
        image3.setOnClickListener { }
        image4.setOnClickListener { }
        image5.setOnClickListener { }
    }

    private fun pointValueChange() {
        val pointValue = FloatArray(100)
        for (i in pointValue.indices) {
            pointValue[i] = Math.random().toFloat() * 2f - 1f
        }
        if (pointChartsRenderer != null) {
            val data = Point2DChartPoint(pointValue)
            surfaceView1.queueEvent {
                pointChartsRenderer?.pointChange(data)
            }
        }
    }

    val list = ArrayList<Float>()
    private fun valueChange() {
        list.add(Math.random().toFloat())
        if (valueChangeRenderer != null) {
            surfaceView1.queueEvent {
                valueChangeRenderer?.valueChange(list.toFloatArray())
            }
        }
    }

    override fun setViewModel(dataBinding: UHFPhaseDataBinding?) {
        dataBinding?.vm = viewModel
    }

    override fun onResume() {
        super.onResume()
        if (rendererSet) {
            surfaceView1.onResume()
            surfaceView2.onResume()
        }
    }

    override fun onPause() {
        super.onPause()
        if (rendererSet) {
            surfaceView1.onPause()
            surfaceView2.onPause()
        }
    }
}