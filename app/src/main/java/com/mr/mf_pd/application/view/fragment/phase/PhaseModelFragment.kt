package com.mr.mf_pd.application.view.fragment.phase

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.fragment.app.viewModels
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.databinding.PhaseDataBinding
import com.mr.mf_pd.application.model.DeviceBean
import com.mr.mf_pd.application.view.base.BaseCheckFragment
import com.mr.mf_pd.application.view.base.BaseFragment
import com.mr.mf_pd.application.view.base.ext.getViewModelFactory
import com.mr.mf_pd.application.view.renderer.PointChartsRenderer
import com.mr.mf_pd.application.view.renderer.PrPsChartsRenderer
import kotlinx.android.synthetic.main.fragment_phase.*
import java.util.*
import kotlin.collections.ArrayList

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
        val value =
            viewModel.checkType.settingBean.maxValue - viewModel.checkType.settingBean.minValue
        val step = value / 4
        val yList = ArrayList<String>()
        for (i in 0..4) {
            val str = viewModel.checkType.settingBean.minValue + step * i
            yList.add(str.toString())
        }
        yList.reverse()
        pointChartsRenderer = PointChartsRenderer(this.requireContext(), yList)
        surfaceView1.setRenderer(pointChartsRenderer)
        surfaceView1.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY

//        pointChartsRenderer?.getPrpsValueCallback =
//            object : PrPsChartsRenderer.GetPrpsValueCallback {
//                override fun getData() {
//                    viewModel.getCaChePhaseData().forEach {
//                        pointChartsRenderer?.addPrpsData(it)
//                    }
//                    viewModel.getPhaseData().forEach {
//                        pointChartsRenderer?.addPrpsData(it)
//                    }
//                }
//            }
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
        image4.setOnClickListener {

        }
        image5.setOnClickListener {
            viewModel.cleanCurrentData()
            pointChartsRenderer?.cleanData()
        }
        rendererSet = true
    }

    override fun setViewModel(dataBinding: PhaseDataBinding?) {
        dataBinding?.vm = viewModel
    }

    override fun onResume() {
        super.onResume()
        if (rendererSet) {
            surfaceView1.onResume()
        }
        viewModel.setRealCallback()
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