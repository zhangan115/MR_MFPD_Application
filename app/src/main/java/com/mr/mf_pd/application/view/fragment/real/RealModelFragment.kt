package com.mr.mf_pd.application.view.fragment.real

import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.fragment.app.viewModels
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.databinding.RealDataBinding
import com.mr.mf_pd.application.model.DeviceBean
import com.mr.mf_pd.application.repository.impl.DataRepository
import com.mr.mf_pd.application.view.base.BaseCheckFragment
import com.mr.mf_pd.application.view.base.ext.getViewModelFactory
import com.mr.mf_pd.application.view.opengl.`object`.PrPsCubeList
import com.mr.mf_pd.application.view.renderer.PrPsChartsRenderer
import kotlinx.android.synthetic.main.fragment_phase.*
import kotlinx.android.synthetic.main.fragment_real.*
import kotlinx.android.synthetic.main.fragment_real.image1
import kotlinx.android.synthetic.main.fragment_real.image2
import kotlinx.android.synthetic.main.fragment_real.image3
import kotlinx.android.synthetic.main.fragment_real.image4
import kotlinx.android.synthetic.main.fragment_real.image5
import kotlinx.android.synthetic.main.fragment_real.surfaceView1

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
        prPsChartsRenderer?.getPrpsValueCallback =
            object : PrPsChartsRenderer.GetPrpsValueCallback {
                override fun getData() {
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
            prPsChartsRenderer?.cleanData()
        }
        image5.setOnClickListener {
            checkActionListener?.changeBandDetectionModel()
        }

        rendererSet = true
    }

    override fun setViewModel(dataBinding: RealDataBinding?) {
        dataBinding?.vm = viewModel
    }


    override fun setCheckFile(str:String) {
        viewModel.ycByteArray = checkActionListener?.getYcByteArray()
        viewModel.setCheckFile(str)
    }

    override fun createACheckFile() {
        viewModel.createACheckFile()
    }

    override fun onResume() {
        super.onResume()
        if (rendererSet) {
            surfaceView1.onResume()
        }
        viewModel.cleanCurrentData()
        prPsChartsRenderer?.cleanData()
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