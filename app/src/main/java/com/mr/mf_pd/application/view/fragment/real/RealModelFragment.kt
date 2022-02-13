package com.mr.mf_pd.application.view.fragment.real

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.fragment.app.viewModels
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.databinding.RealDataBinding
import com.mr.mf_pd.application.model.DeviceBean
import com.mr.mf_pd.application.repository.callback.RealDataCallback
import com.mr.mf_pd.application.repository.impl.DataRepository
import com.mr.mf_pd.application.view.base.BaseCheckFragment
import com.mr.mf_pd.application.view.base.ext.getViewModelFactory
import com.mr.mf_pd.application.view.renderer.PrPsChartsRenderer
import com.mr.mf_pd.application.view.opengl.`object`.PrPsCubeList
import kotlinx.android.synthetic.main.fragment_real.*

class RealModelFragment : BaseCheckFragment<RealDataBinding>() {

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
        surfaceView1.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        viewModel.setRealCallback(object : RealDataCallback {

            override fun onRealDataChanged() {
                viewModel.getPhaseData().forEach {
                    prPsChartsRenderer?.addPrpsData(it)
                }
                viewModel.addHUfData(object : DataRepository.DataCallback {

                    override fun addData(map: HashMap<Int, Float>, prPsCube: PrPsCubeList) {
                        prPsChartsRenderer?.addPrpsData(prPsCube)
                    }
                })
                activity?.runOnUiThread {
                    if (surfaceView1 != null) {
                        surfaceView1.requestRender()
                    }
                }
            }

            override fun onReceiverValueChange() {
                activity?.runOnUiThread {

                }
            }
        })
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
            }
        }

        image2.setOnClickListener {

        }
        image3.setOnClickListener {

        }
        image4.setOnClickListener { }
        image5.setOnClickListener {
            viewModel.cleanCurrentData()
            prPsChartsRenderer?.cleanData()
        }

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

    override fun toSaveData2File() {
        viewModel.isSaveData?.value?.let {
            if (it) {
                viewModel.isSaveData!!.postValue(false)
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