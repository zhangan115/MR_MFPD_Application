package com.mr.mf_pd.application.view.fragment.real

import android.os.Bundle
import androidx.fragment.app.viewModels
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.databinding.RealDataBinding
import com.mr.mf_pd.application.model.DeviceBean
import com.mr.mf_pd.application.repository.impl.DataRepository
import com.mr.mf_pd.application.view.base.BaseFragment
import com.mr.mf_pd.application.view.base.ext.getViewModelFactory
import com.mr.mf_pd.application.view.renderer.PrPsChartsRenderer
import com.mr.mf_pd.application.view.opengl.`object`.PrPsCubeList
import kotlinx.android.synthetic.main.fragment_real.*

class RealModelFragment : BaseFragment<RealDataBinding>() {

    private val viewModel by viewModels<RealModelViewModel> { getViewModelFactory() }
    private var rendererSet = false
    var prPsChartsRenderer: PrPsChartsRenderer? = null

    companion object {

        fun create(deviceBean: DeviceBean?): RealModelFragment {
            val fragment = RealModelFragment()
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
        return R.layout.fragment_real
    }

    override fun initData() {

    }

    override fun initView() {
        surfaceView1.setEGLContextClientVersion(3)
        prPsChartsRenderer = PrPsChartsRenderer(this.requireContext())
        surfaceView1.setRenderer(prPsChartsRenderer)
        prPsChartsRenderer?.getPrpsValueCallback =
            object : PrPsChartsRenderer.GetPrpsValueCallback {
                override fun getData() {
                    viewModel.getCaChePhaseData().forEach {
                        prPsChartsRenderer?.addPrpsData(it)
                    }
                    viewModel.getPhaseData().forEach {
                        prPsChartsRenderer?.addPrpsData(it)
                    }
                    viewModel.addHUfData(object : DataRepository.DataCallback {

                        override fun addData(map: HashMap<Int, Float>, prPsCube: PrPsCubeList) {
                            prPsChartsRenderer?.addPrpsData(prPsCube)
                        }
                    })
                }
            }
        image1.setOnClickListener {

        }

        image2.setOnClickListener {

        }
        image3.setOnClickListener {

        }
        image4.setOnClickListener { }
        image5.setOnClickListener { }

        rendererSet = true
    }

    override fun setViewModel(dataBinding: RealDataBinding?) {
        dataBinding?.vm = viewModel
    }

    override fun onResume() {
        super.onResume()
        if (rendererSet) {
            surfaceView1.onResume()
        }
    }

    override fun onPause() {
        super.onPause()
        if (rendererSet) {
            surfaceView1.onPause()
        }
    }
}