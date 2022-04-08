package com.mr.mf_pd.application.view.check.pulse

import android.graphics.PixelFormat
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.fragment.app.viewModels
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.databinding.ACPulseDataBinding
import com.mr.mf_pd.application.model.SettingBean
import com.mr.mf_pd.application.repository.DefaultDataRepository
import com.mr.mf_pd.application.repository.DefaultFilesRepository
import com.mr.mf_pd.application.view.base.BaseCheckFragment
import com.mr.mf_pd.application.view.base.ext.getViewModelFactory
import com.mr.mf_pd.application.view.callback.FdDataCallback
import kotlinx.android.synthetic.main.fragment_ac_pulse.*
import kotlinx.android.synthetic.main.fragment_ac_pulse.image1
import kotlinx.android.synthetic.main.fragment_ac_pulse.image2
import kotlinx.android.synthetic.main.fragment_ac_pulse.image3
import kotlinx.android.synthetic.main.fragment_ac_pulse.image4
import java.util.concurrent.CopyOnWriteArrayList

/**
 * AC 脉冲波形
 */
class ACPulseModelFragment : BaseCheckFragment<ACPulseDataBinding>() {

    override var TAG = "ACPulseModelFragment"

    private val viewModel by viewModels<ACPulseModelViewModel> { getViewModelFactory() }
    private var rendererSet = false

    companion object {

        fun create(): ACPulseModelFragment {
            val fragment = ACPulseModelFragment()
            val bundle = Bundle()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun lazyLoad() {
        viewModel.start()
    }

    override fun getContentView(): Int {
        return R.layout.fragment_ac_pulse
    }

    override fun initData() {
        mPassageway = viewModel.checkType.passageway
        mCommandType = 4
        if (viewModel.checkType.settingBean.gdCd == 1) {
            viewModel.gainMinValue.postValue(viewModel.checkType.settingBean.minValue.toFloat())
        } else {
            if (viewModel.isFile.value!!) {
                viewModel.gainMinValue.postValue(DefaultFilesRepository.realDataMinValue.value?.toFloat())
            } else {
                viewModel.gainMinValue.postValue(DefaultDataRepository.realDataMinValue.value?.toFloat())
            }
        }
        viewModel.setDataCallback(object : FdDataCallback {
            override fun fdData(data: CopyOnWriteArrayList<Float?>) {
                pulseRenderer?.updateYAxis(getUnitValue(viewModel.checkType.settingBean),
                    getYAxisValue(viewModel.isFile.value!!,
                        viewModel.checkType.settingBean,
                        viewModel.gainMinValue))
                pulseRenderer?.updateData(data)
            }
        })
    }

    override fun setIsFileValue(isFile: Boolean?) {
        viewModel.isFile.value = isFile
    }

    var pulseRenderer: PulseRenderer? = null

    override fun initView() {
        activity?.let {
            surfaceView.setEGLContextClientVersion(3)
            pulseRenderer = PulseRenderer(this.requireContext(),
                viewModel.getQueue(),
                viewModel.pulseValueCallBack)
            surfaceView.setRenderer(pulseRenderer)
            surfaceView.setZOrderOnTop(true)
            surfaceView.holder.setFormat(PixelFormat.TRANSPARENT)
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
    }

    override fun setViewModel(dataBinding: ACPulseDataBinding?) {
        dataBinding?.vm = viewModel
    }

    override fun isSaving(): Boolean {
        return false
    }

    override fun cancelSaveData() {
        viewModel.isSaveData?.postValue(false)
    }


    override fun onYcDataChange(bytes: ByteArray) {

    }

    override fun cleanCurrentData() {
        pulseRenderer?.cleanData()
        viewModel.cleanCurrentData()
    }

    override fun isAdd(): Boolean {
        return isAdded
    }

    override fun updateSettingBean(settingBean: SettingBean) {
        viewModel.checkType.settingBean = settingBean
    }

    override fun onResume() {
        super.onResume()
        if (rendererSet) {
            surfaceView.onResume()
        }
        viewModel.onResume()
        cleanCurrentData()
    }

    override fun onPause() {
        super.onPause()
        if (rendererSet) {
            surfaceView.onPause()
        }
        viewModel.onPause()
    }

    override fun setCheckFile(str: String) {
        viewModel.setCheckFile(str)
    }

    override fun createCheckFile() {
        viewModel.createACheckFile()
    }

}