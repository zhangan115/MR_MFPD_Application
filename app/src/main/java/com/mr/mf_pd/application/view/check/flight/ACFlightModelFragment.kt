package com.mr.mf_pd.application.view.check.flight

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.fragment.app.viewModels
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.databinding.ACFlightDataBinding
import com.mr.mf_pd.application.model.EventObserver
import com.mr.mf_pd.application.repository.DefaultDataRepository
import com.mr.mf_pd.application.repository.DefaultFilesRepository
import com.mr.mf_pd.application.view.base.BaseCheckFragment

import com.mr.mf_pd.application.view.base.ext.getViewModelFactory
import com.mr.mf_pd.application.view.renderer.PointChartsRenderer
import kotlinx.android.synthetic.main.fragment_ac_flight.*
import kotlinx.android.synthetic.main.fragment_ac_flight.image1
import kotlinx.android.synthetic.main.fragment_ac_flight.image2
import kotlinx.android.synthetic.main.fragment_ac_flight.image3
import kotlinx.android.synthetic.main.fragment_ac_flight.image4
import kotlinx.android.synthetic.main.fragment_ac_flight.surfaceView1
import kotlinx.android.synthetic.main.fragment_phase.*

/**
 * AC 飞行模式
 */
class ACFlightModelFragment : BaseCheckFragment<ACFlightDataBinding>() {

    private val viewModel by viewModels<ACFlightModelViewModel> { getViewModelFactory() }
    var pointChartsRenderer: PointChartsRenderer? = null
    private var rendererSet = false

    companion object {

        fun create(): ACFlightModelFragment {
            val fragment = ACFlightModelFragment()
            val bundle = Bundle()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun lazyLoad() {
        viewModel.start()
    }

    override fun getContentView(): Int {
        return R.layout.fragment_ac_flight
    }

    override fun initData() {
        if (viewModel.checkType.settingBean.gdCd == 1) {
            viewModel.gainMinValue.postValue(viewModel.checkType.settingBean.minValue.toFloat())
        } else {
            if (viewModel.isFile.value!!) {
                viewModel.gainMinValue.postValue(DefaultFilesRepository.realDataMinValue.value?.toFloat())
            } else {
                viewModel.gainMinValue.postValue(DefaultDataRepository.realDataMinValue.value?.toFloat())
            }
        }
        viewModel.toFlightDataEvent.observe(this, EventObserver {
            pointChartsRenderer?.setFlightData(it)
            pointChartsRenderer?.updateYAxis(getYAxisValue(viewModel.isFile.value!!,
                viewModel.checkType.settingBean,
                viewModel.gainMinValue))
            surfaceView1.requestRender()
        })
    }

    override fun setIsFileValue(isFile: Boolean?) {
        viewModel.isFile.value = isFile
    }

    override fun initView() {
        surfaceView1.setEGLContextClientVersion(3)
        pointChartsRenderer = PointChartsRenderer(this.requireContext(),
            getYAxisValue(viewModel.isFile.value!!,
                viewModel.checkType.settingBean,
                viewModel.gainMinValue))
        surfaceView1.setRenderer(pointChartsRenderer)
        surfaceView1.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
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
        rendererSet = true
    }

    override fun onYcDataChange(bytes: ByteArray) {
        val valueList = splitBytesToValue(bytes)
    }

    override fun cleanCurrentData() {
        pointChartsRenderer?.cleanData()
        viewModel.cleanCurrentData()
    }

    override fun setViewModel(dataBinding: ACFlightDataBinding?) {
        dataBinding?.vm = viewModel
    }


    override fun onResume() {
        super.onResume()
        if (rendererSet) {
            surfaceView1.onResume()
        }
        cleanCurrentData()
    }

    override fun onPause() {
        super.onPause()
        if (rendererSet) {
            surfaceView1.onPause()
        }
    }

    override fun isSaving(): Boolean {
        return false
    }

    override fun cancelSaveData() {
        viewModel.isSaveData?.postValue(false)
    }

    override fun isAdd(): Boolean {
        return isAdded
    }

}