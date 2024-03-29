package com.mr.mf_pd.application.view.check.flight

import android.graphics.PixelFormat
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.fragment.app.viewModels
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.databinding.ACFlightDataBinding
import com.mr.mf_pd.application.model.SettingBean
import com.mr.mf_pd.application.repository.DefaultDataRepository
import com.mr.mf_pd.application.repository.DefaultFilesRepository
import com.mr.mf_pd.application.view.base.BaseCheckFragment
import com.mr.mf_pd.application.view.base.ext.getViewModelFactory
import com.mr.mf_pd.application.view.callback.FlightDataCallback
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_ac_flight.*

/**
 * AC 飞行模式
 */
class ACFlightModelFragment : BaseCheckFragment<ACFlightDataBinding>() {

    override var TAG = "ACFlightModelFragment"

    private val viewModel by viewModels<ACFlightModelViewModel> { getViewModelFactory() }
    var flightChartsRenderer: FlightChartsRenderer? = null
    private var rendererSet = false

    companion object {

        fun create(isFile: Boolean): ACFlightModelFragment {
            val fragment = ACFlightModelFragment()
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
        return R.layout.fragment_ac_flight
    }

    override fun oneSecondUIChange() {
        gainChartView?.updateFzValue()
    }

    private var disposable: Disposable? = null

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
        viewModel.setFlightCallback(object : FlightDataCallback {
            override fun flightData(data: HashMap<Int, HashMap<Float, Int>>, xMaxValue: Int) {
                flightChartsRenderer?.setFlightData(data)
                flightChartsRenderer?.updateYAxis(
                    getUnitValue(viewModel.checkType.settingBean),
                    getYAxisValue(viewModel.isFile.value!!,
                        viewModel.checkType.settingBean,
                        viewModel.gainMinValue),
                    xMaxValue
                )
            }
        })

        viewModel.toResetEvent.observe(this) {
            cleanCurrentData()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
    }

    override fun updateLjView() {
        flightChartsRenderer?.cleanData()
    }

    override fun onLimitValueChange(value: Int) {
        viewModel.limitValueStr.postValue("通道门限值:$value")
    }

    override fun setIsFileValue(isFile: Boolean?) {
        viewModel.isFile.value = isFile
    }

    override fun initView() {
        surfaceView1.setEGLContextClientVersion(3)
        flightChartsRenderer = FlightChartsRenderer(this.requireContext(),
            viewModel.getQueue(),
            viewModel.flightValueCallBack)
        surfaceView1.setRenderer(flightChartsRenderer)
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
        locationLayout.setOnClickListener {
            createChooseFileIntent()
        }
        rendererSet = true
    }

    override fun setCheckFile(str: String) {
        viewModel.setCheckFile(str)
    }

    override fun createCheckFile() {
        viewModel.createACheckFile()
    }

    override fun onYcDataChange(bytes: ByteArray) {
//       val valueList =  splitBytesToValue(bytes)
    }

    override fun fdStrChange(fdType: String?) {
        viewModel.setState(fdType)
    }

    override fun cleanCurrentData() {
        flightChartsRenderer?.cleanData()
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

    override fun updateSettingBean(settingBean: SettingBean) {
        viewModel.checkType.settingBean = settingBean
    }

}