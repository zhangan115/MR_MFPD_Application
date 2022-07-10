package com.mr.mf_pd.application.view.check.real

import android.graphics.PixelFormat
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.fragment.app.viewModels
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.databinding.RealDataBinding
import com.mr.mf_pd.application.model.SettingBean
import com.mr.mf_pd.application.repository.DefaultDataRepository
import com.mr.mf_pd.application.repository.DefaultFilesRepository
import com.mr.mf_pd.application.view.base.BaseCheckFragment
import com.mr.mf_pd.application.view.base.ext.getViewModelFactory
import com.mr.mf_pd.application.view.callback.PrPsDataCallback
import kotlinx.android.synthetic.main.fragment_real.*
import java.text.DecimalFormat
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 实时数据 实时模式
 */
class RealModelFragment : BaseCheckFragment<RealDataBinding>() {

    override var TAG = "RealModelFragment"

    private val viewModel by viewModels<RealModelViewModel> { getViewModelFactory() }
    private var rendererSet = false
    var prPsChartsRenderer: PrPsChartsRenderer? = null

    companion object {

        fun create(isFile: Boolean): RealModelFragment {
            val fragment = RealModelFragment()
            val bundle = Bundle()
            bundle.putBoolean(ConstantStr.KEY_BUNDLE_BOOLEAN, isFile)
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
        checkType = viewModel.checkType
        if (viewModel.checkType.settingBean.gdCd == 1) {
            viewModel.gainMinValue.postValue(viewModel.checkType.settingBean.minValue.toFloat())
        } else {
            if (viewModel.isFile.value!!) {
                viewModel.gainMinValue.postValue(DefaultFilesRepository.realDataMinValue.value?.toFloat())
            } else {
                viewModel.gainMinValue.postValue(DefaultDataRepository.realDataMinValue.value?.toFloat())
            }
        }
        viewModel.prPsDataCallback = object : PrPsDataCallback {
            override fun prpsDataChange(
                data: ConcurrentHashMap<Int, ConcurrentHashMap<Float, Int>>,
                cubeList: CopyOnWriteArrayList<Float?>,
            ) {
                prPsChartsRenderer?.updateAxis(getUnitValue(viewModel.checkType.settingBean),
                    getYAxisValue(viewModel.isFile.value!!,
                        viewModel.checkType.settingBean,
                        viewModel.gainMinValue))
                prPsChartsRenderer?.updatePrpsData(data, cubeList)
            }
        }
        viewModel.toResetEvent.observe(this) {
            cleanCurrentData()
        }
    }

    override fun updateLjView() {

    }

    override fun initView() {
        surfaceView1.setEGLContextClientVersion(3)
        prPsChartsRenderer =
            PrPsChartsRenderer(this.requireContext(),
                checkType == CheckType.HF,
                checkType!!.settingBean,
                viewModel.getQueue(),
                viewModel.realBytesDataCallback)
        surfaceView1.setRenderer(prPsChartsRenderer)
        surfaceView1.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        surfaceView1.setZOrderOnTop(true)
        surfaceView1.holder.setFormat(PixelFormat.TRANSPARENT)
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
                if (viewModel.filesRepository.getCurrentCheckName() != null) {
                    location = viewModel.filesRepository.getCurrentCheckName()
                }
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
//        if (viewModel.checkType == CheckType.UHF) {
//            image5.visibility = View.VISIBLE
//        } else {
//            image5.visibility = View.GONE
//        }
        rendererSet = true
    }

    override fun onLimitValueChange(value: Int) {
        viewModel.limitValueStr.postValue("通道门限值:$value")
    }

    override fun setViewModel(dataBinding: RealDataBinding?) {
        dataBinding?.vm = viewModel
    }

    override fun setCheckFile(str: String) {
        viewModel.setCheckFile(str)
    }

    override fun createCheckFile() {
        viewModel.createACheckFile()
    }

    override fun onYcDataChange(bytes: ByteArray) {
        val valueList = splitBytesToValue(bytes)
        if (valueList.size >= 2) {
            view?.let {
                val df1 = DecimalFormat("0.00")
                viewModel.checkType.checkParams.value?.let {
                    it.hzAttr = df1.format(valueList[1])
                    if (!viewModel.canUpdateFz) {
                        val fzValue = valueList.lastOrNull()
                        it.fzAttr = df1.format(fzValue) + viewModel.checkType.settingBean.fzUnit
                    }
                    viewModel.updateFzValue(it)
                }
            }
        }
    }

    override fun fdStrChange(fdType: String?) {
        viewModel.setState(fdType)
    }

    override fun oneSecondUIChange() {
        gainChartView?.updateFzValue()
    }

    override fun cleanCurrentData() {
        viewModel.cleanCurrentData()
        prPsChartsRenderer?.cleanData()
        initChartView(viewModel.checkType.settingBean)
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

    override fun setIsFileValue(isFile: Boolean?) {
        viewModel.isFile.value = isFile
    }

    override fun updateSettingBean(settingBean: SettingBean) {
        viewModel.checkType.settingBean = settingBean
        initChartView(settingBean)
    }

    private fun initChartView(settingBean: SettingBean){
        prPsChartsRenderer?.updateAxis(getUnitValue(settingBean),
            getYAxisValue(viewModel.isFile.value!!,
                settingBean,
                viewModel.gainMinValue))
        surfaceView1.requestRender()
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
}