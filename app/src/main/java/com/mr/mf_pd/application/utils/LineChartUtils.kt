package com.mr.mf_pd.application.utils

import android.content.Context
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.app.MRApplication
import kotlin.math.min

/**
 * 图标帮助类型
 * @author zhangan
 * @since 2022-02-11
 */
object LineChartUtils {

    fun initNoAxisChart(lineChart: LineChart) {
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

    fun initChart(lineChart: LineChart, content: Context) {
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
        lineChart.axisRight.isEnabled = false

        lineChart.xAxis.isEnabled = true
        lineChart.xAxis.textColor = content.resources.getColor(R.color.text_black, null)
        lineChart.xAxis.axisLineColor = content.resources.getColor(R.color.chart_xy_color, null)
        lineChart.xAxis.gridColor = content.resources.getColor(R.color.chart_line_color, null)
        lineChart.xAxis.setLabelCount(5, true)

        lineChart.axisLeft.setDrawZeroLine(false)
        lineChart.axisLeft.setDrawGridLines(true)
        lineChart.axisLeft.isEnabled = true
        lineChart.axisLeft.textColor = content.resources.getColor(R.color.text_black, null)
        lineChart.axisLeft.axisLineColor = content.resources.getColor(R.color.chart_xy_color, null)
        lineChart.axisLeft.gridColor = content.resources.getColor(R.color.chart_line_color, null)
        lineChart.axisLeft.setLabelCount(5, false)
    }

    private fun initDataSet(dataSet: LineDataSet, color: Int, highColor: Int) {
        dataSet.lineWidth = 2.0f
        dataSet.fillColor = color
        dataSet.fillAlpha = 255
        dataSet.setDrawFilled(true)
        dataSet.color = color
        dataSet.setDrawCircleHole(false)
        dataSet.setDrawValues(false)
        dataSet.setDrawCircles(false)
        dataSet.mode = LineDataSet.Mode.LINEAR
        dataSet.highLightColor = highColor
    }

    fun updateData(
        lineChart: LineChart,
        list: ArrayList<Float>,
        minValue: Float? = null,
        maxValue: Float? = null,
    ) {
        lineChart.data = getLineData(list)
        if (minValue!=null){
            lineChart.axisLeft.axisMinimum = minValue
        }
        if (maxValue!=null){
            lineChart.axisLeft.axisMaximum = maxValue
        }
        lineChart.invalidate()
    }

    private fun getLineData(chartDataList: List<Float>): LineData {
        val dataSets: MutableList<ILineDataSet> =
            ArrayList()
        val entries: MutableList<Entry> =
            ArrayList()
        if (chartDataList.isNotEmpty()) {
            for (i in chartDataList.indices) {
                entries.add(Entry(i.toFloat(), chartDataList[i]))
            }
            val dataSet = LineDataSet(entries, "")
            initDataSet(dataSet,
                MRApplication.instance.getColor(R.color.blueColor),
                MRApplication.instance.getColor(R.color.blueColor))
            dataSets.add(dataSet)
        }
        return LineData(dataSets)
    }


}