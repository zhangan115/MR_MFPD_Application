package com.mr.mf_pd.application.view.fragment.real

import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.fragment.app.viewModels
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.databinding.RealDataBinding
import com.mr.mf_pd.application.model.DeviceBean
import com.mr.mf_pd.application.repository.DefaultDataRepository
import com.mr.mf_pd.application.repository.DefaultFilesRepository
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

        fun create(isFile:Boolean): RealModelFragment {
            val fragment = RealModelFragment()
            val bundle = Bundle()
            bundle.putBoolean(ConstantStr.KEY_BUNDLE_BOOLEAN,isFile)
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
        if (viewModel.checkType.settingBean.gdCd == 1){
            viewModel.gainMinValue.postValue(viewModel.checkType.settingBean.minValue.toFloat())
        }else{
            if (viewModel.isFile.value!!) {
                viewModel.gainMinValue.postValue(DefaultFilesRepository.realDataMinValue.value?.toFloat())
            } else {
                viewModel.gainMinValue.postValue(DefaultDataRepository.realDataMinValue.value?.toFloat())
            }
        }
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
            cleanCurrentData()
        }
        image5.setOnClickListener {
            checkActionListener?.changeBandDetectionModel()
        }
        if (viewModel.checkType == CheckType.UHF){
            image5.visibility = View.VISIBLE
        }else{
            image5.visibility = View.GONE
        }
        rendererSet = true
    }

    override fun setViewModel(dataBinding: RealDataBinding?) {
        dataBinding?.vm = viewModel
    }


    override fun setCheckFile(str:String) {
        viewModel.setCheckFile(str)
    }

    override fun createCheckFile() {
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

    override fun onYcDataChange(bytes: ByteArray) {
        val valueList = splitBytesToValue(bytes)
        if (valueList.size >= 2) {
            view?.let {
                viewModel.checkType.checkParams.value?.hzAttr = valueList[1].toString()
                viewModel.checkType.checkParams.postValue(viewModel.checkType.checkParams.value)
            }
        }
    }

    override fun cleanCurrentData() {
        viewModel.cleanCurrentData()
        prPsChartsRenderer?.cleanData()
    }

    override fun isSaving(): Boolean {
        return if (viewModel.isSaveData == null) false else viewModel.isSaveData!!.value!!
    }

    override fun cancelSaveData() {
        viewModel.isSaveData?.postValue(false)
    }

    override fun isAdd(): Boolean {
        return isAdded
    }

}