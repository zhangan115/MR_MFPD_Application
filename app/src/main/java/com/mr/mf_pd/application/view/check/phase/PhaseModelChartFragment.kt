package com.mr.mf_pd.application.view.check.phase

import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.fragment.app.viewModels
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.databinding.PhaseDataChartBinding
import com.mr.mf_pd.application.manager.socket.SocketManager
import com.mr.mf_pd.application.model.SettingBean
import com.mr.mf_pd.application.repository.DefaultDataRepository
import com.mr.mf_pd.application.repository.DefaultFilesRepository
import com.mr.mf_pd.application.view.base.BaseCheckFragment
import com.mr.mf_pd.application.view.base.ext.getViewModelFactory
import kotlinx.android.synthetic.main.fragment_phase_1.*
import java.text.DecimalFormat

class PhaseModelChartFragment : BaseCheckFragment<PhaseDataChartBinding>() {
    override var TAG = "PhaseModelChartFragment"

    private val viewModel by viewModels<PhaseModelViewModel> { getViewModelFactory() }
    private var rendererSet = false

    companion object {

        fun create(isFile: Boolean): PhaseModelChartFragment {
            val fragment = PhaseModelChartFragment()
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
        return R.layout.fragment_phase_1
    }

    override fun initData() {
        mPassageway = viewModel.checkType.passageway
        checkType = viewModel.checkType
        mCommandType = 1
        if (viewModel.checkType.settingBean.gdCd == 1) {
            viewModel.gainMinValue.postValue(viewModel.checkType.settingBean.minValue.toFloat())
        } else {
            if (viewModel.isFile.value!!) {
                viewModel.gainMinValue.postValue(DefaultFilesRepository.realDataMinValue.value?.toFloat())
            } else {
                viewModel.gainMinValue.postValue(DefaultDataRepository.realDataMinValue.value?.toFloat())
            }
        }
        mrChartView.drawSinLines = true
        mrChartView.unit = checkType?.settingBean?.fzUnit
        mrChartView.dataCallback = viewModel.realBytesDataCallback
        viewModel.dataCallback = {
            mrChartView.setValue(it)
        }
        mrChartView.mQueue = SocketManager.get().realDataDeque
    }

    override fun initView() {
        mrChartView.setZOrderOnTop(true)
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
        if (viewModel.checkType == CheckType.HF || viewModel.checkType == CheckType.TEV) {
            image5.visibility = View.GONE
        }
        rendererSet = true
    }

    override fun setCheckFile(str: String) {
        viewModel.setCheckFile(str)
    }

    override fun createCheckFile() {
        viewModel.createACheckFile()
    }

    override fun setViewModel(dataBinding: PhaseDataChartBinding?) {
        dataBinding?.vm = viewModel
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
        cleanCurrentData()
    }

    override fun onPause() {
        super.onPause()
        viewModel.onPause()
    }

    override fun onYcDataChange(bytes: ByteArray) {
        val valueList = splitBytesToValue(bytes)
        if (valueList.size >= 2) {
            view?.let {
                val df1 = DecimalFormat("0.00")
                viewModel.checkType.checkParams.value?.let {
                    it.hzAttr = df1.format(valueList[1])
                    if ((viewModel.checkType == CheckType.TEV || viewModel.checkType == CheckType.AE || viewModel.checkType == CheckType.AA) && valueList.size >= 6) {
                        it.effectiveValueAttr = df1.format(valueList[3])
                    }
                    if (!viewModel.canUpdateFz) {
                        val fzValue = valueList.lastOrNull()
                        it.fzAttr = df1.format(fzValue) + viewModel.checkType.settingBean.fzUnit
                    }
                    viewModel.updateFzValue(it)
                }
            }
        }
    }

    override fun onLimitValueChange(value: Int) {
        viewModel.limitValueStr.postValue("通道门限值:$value")
    }

    override fun cleanCurrentData() {
        viewModel.cleanCurrentData()
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
        mrChartView.unit = settingBean.fzUnit
    }
}