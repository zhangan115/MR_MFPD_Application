package com.mr.mf_pd.application.view.uhf.phase

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.viewModels
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.databinding.UHFPhaseDataBinding
import com.mr.mf_pd.application.model.DeviceBean
import com.mr.mf_pd.application.view.base.BaseFragment
import com.mr.mf_pd.application.view.base.ext.getViewModelFactory
import com.mr.mf_pd.application.view.uhf.phase.renderer.PointChartsRenderer
import com.mr.mf_pd.application.view.uhf.phase.renderer.ValueChangeRenderer
import com.sito.tool.library.utils.DisplayUtil
import kotlinx.android.synthetic.main.fragment_uhf_phase.*

class UHFPhaseModelFragment : BaseFragment<UHFPhaseDataBinding>() {

    private val viewModel by viewModels<UHFPhaseModelViewModel> { getViewModelFactory() }
    private var rendererSet = false

    companion object {

        fun create(deviceBean: DeviceBean?): UHFPhaseModelFragment {
            val fragment = UHFPhaseModelFragment()
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
        return R.layout.fragment_uhf_phase
    }

    override fun initData() {

    }

    var thrend: Thread? = null
    var pointChartsRenderer: PointChartsRenderer? = null
    var valueChangeRenderer: ValueChangeRenderer? = null

    override fun initView() {
        surfaceView1.setEGLContextClientVersion(3)
        surfaceView2.setEGLContextClientVersion(3)
        pointChartsRenderer = PointChartsRenderer(this.requireContext())
        val width = requireContext().resources.displayMetrics.widthPixels - DisplayUtil.dip2px(
            requireContext(),
            (80f + 12f)
        )
        val height = DisplayUtil.dip2px(requireContext(), 75f)
        valueChangeRenderer =
            ValueChangeRenderer(this.requireContext(), width, height)

        surfaceView1.setRenderer(pointChartsRenderer)
        surfaceView2.setRenderer(valueChangeRenderer)

        image1.setOnClickListener {
            thrend = Thread(kotlinx.coroutines.Runnable {
                while (true) {
                    Thread.sleep(20)
                    pointValueChange()
                }
            })
            thrend?.start()
        }

        image2.setOnClickListener {
//            Thread(kotlinx.coroutines.Runnable {
//                while (true) {
//                    Thread.sleep(2000)
//                    valueChange()
//                }
//            }).start()
            valueChange()
        }
        image3.setOnClickListener { }
        image4.setOnClickListener { }
        image5.setOnClickListener { }
    }

    private fun pointValueChange() {
        val pointValue = FloatArray(100)
        for (i in pointValue.indices) {
            pointValue[i] = Math.random().toFloat() * 2f - 1f
        }
        if (pointChartsRenderer != null) {
            surfaceView1.queueEvent {
                pointChartsRenderer?.pointChange(pointValue)
            }
        }
    }

    val list = ArrayList<Float>()
    private fun valueChange() {
        list.add(Math.random().toFloat())
        if (valueChangeRenderer != null) {
            surfaceView1.queueEvent {
                valueChangeRenderer?.valueChange(list.toFloatArray())
            }
        }
    }

    override fun setViewModel(dataBinding: UHFPhaseDataBinding?) {
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