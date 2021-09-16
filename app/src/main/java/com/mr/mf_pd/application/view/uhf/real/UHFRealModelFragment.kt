package com.mr.mf_pd.application.view.uhf.real

import android.os.Bundle
import androidx.fragment.app.viewModels
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.databinding.UHFRealDataBinding
import com.mr.mf_pd.application.model.DeviceBean
import com.mr.mf_pd.application.view.base.BaseFragment
import com.mr.mf_pd.application.view.base.ext.getViewModelFactory
import com.mr.mf_pd.application.view.opengl.AirHockey3DRenderer
import com.mr.mf_pd.application.view.opengl.`object`.Point2DChartPoint
import com.mr.mf_pd.application.view.opengl.`object`.PrPsCube
import com.mr.mf_pd.application.view.opengl.`object`.PrPsXZPoints
import com.mr.mf_pd.application.view.uhf.renderer.PrPsChartsRenderer
import com.mr.mf_pd.application.view.uhf.renderer.ValueChangeRenderer
import kotlinx.android.synthetic.main.fragment_uhf_phase.*
import kotlinx.android.synthetic.main.fragment_uhf_real.*
import kotlinx.android.synthetic.main.fragment_uhf_real.image1
import kotlinx.android.synthetic.main.fragment_uhf_real.image2
import kotlinx.android.synthetic.main.fragment_uhf_real.image3
import kotlinx.android.synthetic.main.fragment_uhf_real.image4
import kotlinx.android.synthetic.main.fragment_uhf_real.image5
import kotlinx.android.synthetic.main.fragment_uhf_real.surfaceView1
import kotlinx.android.synthetic.main.fragment_uhf_real.surfaceView2

class UHFRealModelFragment : BaseFragment<UHFRealDataBinding>() {

    private val viewModel by viewModels<UHFRealModelViewModel> { getViewModelFactory() }
    private var rendererSet = false
    var valueChangeRenderer: ValueChangeRenderer? = null
    var prPsChartsRenderer: PrPsChartsRenderer? = null

    private var prPsCubeList: ArrayList<ArrayList<PrPsCube>> = ArrayList()

    companion object {

        fun create(deviceBean: DeviceBean?): UHFRealModelFragment {
            val fragment = UHFRealModelFragment()
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
        return R.layout.fragment_uhf_real
    }

    override fun initData() {

    }

    override fun initView() {
        surfaceView1.setEGLContextClientVersion(3)
        surfaceView2.setEGLContextClientVersion(3)
        valueChangeRenderer =
            ValueChangeRenderer(this.requireContext())
        prPsChartsRenderer = PrPsChartsRenderer(this.requireContext())
        surfaceView1.setRenderer(prPsChartsRenderer)
        surfaceView2.setRenderer(valueChangeRenderer)

        image1.setOnClickListener {
            Thread(kotlinx.coroutines.Runnable {
                val list = ArrayList<PrPsCube>()
                val pointValue = FloatArray(100)
                for (i in pointValue.indices) {
                    pointValue[i] = Math.random().toFloat() * 2f - 1f
                    list.add(PrPsCube(pointValue[i]))
                }
                val data = PrPsXZPoints(pointValue)
                if (prPsCubeList.size==50){
                    prPsCubeList.removeLast()
                }
                prPsCubeList.add(0,list)

                surfaceView1.queueEvent {
                    prPsChartsRenderer?.pointChange(data,prPsCubeList)
                }

            }).start()
        }

        image2.setOnClickListener {
            valueChange()
        }
        image3.setOnClickListener {

        }
        image4.setOnClickListener { }
        image5.setOnClickListener { }
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

    override fun setViewModel(dataBinding: UHFRealDataBinding?) {
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