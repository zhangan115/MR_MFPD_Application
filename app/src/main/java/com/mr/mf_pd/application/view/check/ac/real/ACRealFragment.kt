package com.mr.mf_pd.application.view.check.ac.real

import android.os.Bundle
import androidx.fragment.app.viewModels
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.databinding.ACRealDataBinding
import com.mr.mf_pd.application.model.DeviceBean
import com.mr.mf_pd.application.repository.impl.DataRepository
import com.mr.mf_pd.application.view.base.BaseFragment
import com.mr.mf_pd.application.view.base.ext.getViewModelFactory
import com.mr.mf_pd.application.view.check.uhf.renderer.PrPsChartsRenderer
import com.mr.mf_pd.application.view.check.uhf.renderer.ValueChangeRenderer
import com.mr.mf_pd.application.view.opengl.`object`.PrPsCubeList
import kotlinx.android.synthetic.main.fragment_ac_real.*

/**
 * AC 实时模式
 */
class ACRealFragment : BaseFragment<ACRealDataBinding>() {

    private val viewModel by viewModels<ACRealModelViewModel> { getViewModelFactory() }
    private var rendererSet = false
    var valueChangeRenderer: ValueChangeRenderer? = null
    var prPsChartsRenderer: PrPsChartsRenderer? = null

    companion object {

        fun create(deviceBean: DeviceBean?): ACRealFragment {
            val fragment = ACRealFragment()
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
        return R.layout.fragment_ac_real
    }

    override fun initData() {

    }

    override fun initView() {
        surfaceView1.setEGLContextClientVersion(3)
        surfaceView2.setEGLContextClientVersion(3)
        valueChangeRenderer =
            ValueChangeRenderer(this.requireContext())
        prPsChartsRenderer = PrPsChartsRenderer(this.requireContext())
        surfaceView1.setRenderer(prPsChartsRenderer)
        surfaceView2.setRenderer(valueChangeRenderer)
        prPsChartsRenderer?.getPrpsValueCallback =
            object : PrPsChartsRenderer.GetPrpsValueCallback {
                override fun getData() {
                    viewModel.addACData(object : DataRepository.DataCallback {

                        override fun addData(map: HashMap<Int, Float>, prPsCube: PrPsCubeList) {
                            prPsChartsRenderer?.addPrpsData(map, prPsCube)
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
    }

    override fun setViewModel(dataBinding: ACRealDataBinding?) {
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