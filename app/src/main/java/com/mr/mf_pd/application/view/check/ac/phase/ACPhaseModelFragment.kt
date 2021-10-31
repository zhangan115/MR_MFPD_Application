package com.mr.mf_pd.application.view.check.ac.phase

import android.os.Bundle
import androidx.fragment.app.viewModels
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.databinding.ACPhaseDataBinding
import com.mr.mf_pd.application.model.DeviceBean
import com.mr.mf_pd.application.view.base.BaseFragment
import com.mr.mf_pd.application.view.base.ext.getViewModelFactory
import com.mr.mf_pd.application.view.renderer.PointChartsRenderer
import com.mr.mf_pd.application.view.renderer.ValueChangeRenderer
import com.mr.mf_pd.application.view.renderer.PrPsChartsRenderer
import kotlinx.android.synthetic.main.fragment_ac_phase.*
/**
 * AC 相位模式
 */
class ACPhaseModelFragment : BaseFragment<ACPhaseDataBinding>() {

    private val viewModel by viewModels<ACPhaseModelViewModel> { getViewModelFactory() }
    private var rendererSet = false

    var pointChartsRenderer: PointChartsRenderer? = null
    var valueChangeRenderer: ValueChangeRenderer? = null

    companion object {

        fun create(deviceBean: DeviceBean?): ACPhaseModelFragment {
            val fragment = ACPhaseModelFragment()
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
        return R.layout.fragment_ac_phase
    }

    override fun initData() {

    }


    override fun initView() {
        surfaceView1.setEGLContextClientVersion(3)
        surfaceView2.setEGLContextClientVersion(3)
        pointChartsRenderer = PointChartsRenderer(this.requireContext())
        valueChangeRenderer =
            ValueChangeRenderer(this.requireContext())

        surfaceView1.setRenderer(pointChartsRenderer)
        surfaceView2.setRenderer(valueChangeRenderer)

        pointChartsRenderer?.getPrpsValueCallback =
            object : PrPsChartsRenderer.GetPrpsValueCallback {
                override fun getData() {

                    viewModel.getCaChePhaseData().forEach {
                        pointChartsRenderer?.addPrpsData(it)
                    }
                    viewModel.getPhaseData().forEach {
                        pointChartsRenderer?.addPrpsData(it)
                    }
                }
            }

        image1.setOnClickListener {

        }

        image2.setOnClickListener {

        }
        image3.setOnClickListener { }
        image4.setOnClickListener { }
        image5.setOnClickListener { }

        rendererSet = true
    }

    override fun setViewModel(dataBinding: ACPhaseDataBinding?) {
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