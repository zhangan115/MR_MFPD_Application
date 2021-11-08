package com.mr.mf_pd.application.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.mr.mf_pd.application.R
import kotlinx.android.synthetic.main.layout_gain_chart.view.*
import kotlin.math.max
import kotlin.math.min

class GainChartView : LinearLayout {

    constructor(context: Context?) : super(context!!) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    ) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context!!, attrs, defStyle
    ) {
        init()
    }

    private fun init() {
        inflate(context, R.layout.layout_gain_chart, this)
        initCharts()
    }

    private fun initCharts() {
        lineChart.clear()
        lineChart.description = null
        lineChart.setNoDataText("")
        lineChart.alpha = 1f
        lineChart.setMaxVisibleValueCount(1000)
        lineChart.minOffset = 0f
        lineChart.setTouchEnabled(false)
        lineChart.isDragEnabled = false
        lineChart.setScaleEnabled(false)
        lineChart.setPinchZoom(false)
        lineChart.legend.isEnabled = false
        lineChart.xAxis.isEnabled = false
        lineChart.axisLeft.setDrawZeroLine(false)
        lineChart.axisLeft.isEnabled = false
        lineChart.axisRight.isEnabled = false
    }

    private fun getLineData(chartDataList: List<Float>): LineData {
        val dataSets: MutableList<ILineDataSet> =
            ArrayList()
        val entries: MutableList<Entry> =
            ArrayList()
        if (chartDataList.isNotEmpty()) {
            var max = chartDataList.first()
            var min = chartDataList.first()
            for (i in chartDataList.indices) {
                entries.add(Entry(i.toFloat(), chartDataList[i]))
                min = min(min, chartDataList[i])
                max = max(max, chartDataList[i])
            }
            val dataSet = LineDataSet(entries, "")
            initDataSet(dataSet)
            dataSets.add(dataSet)
            num1.text = max.toString()
            num2.text = chartDataList.last().toString()
            num3.text = min.toString()
        }
        return LineData(dataSets)
    }


    private fun initDataSet(dataSet: LineDataSet) {
        dataSet.lineWidth = 2.0f
        dataSet.fillColor = context.getColor(R.color.blueColor)
        dataSet.fillAlpha = 255
        dataSet.setDrawFilled(true)
        dataSet.color = context.getColor(R.color.blueColor)
        dataSet.setDrawCircleHole(false)
        dataSet.setDrawValues(false)
        dataSet.setDrawCircles(false)
        dataSet.mode = LineDataSet.Mode.LINEAR
        dataSet.highLightColor = context.getColor(R.color.colorTransparent)
    }

    fun updateData(list: List<Float>) {
        lineChart.data = getLineData(list)
        lineChart.invalidate()
    }

}