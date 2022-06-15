package com.mr.mf_pd.application.widget

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.LinearLayout
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.utils.LineChartUtils
import kotlinx.android.synthetic.main.layout_gain_chart.view.*
import kotlinx.coroutines.MainScope
import java.math.BigDecimal
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.max
import kotlin.math.min

open class GainChartView : LinearLayout {

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
        LineChartUtils.initNoAxisChart(lineChart)
    }

    @Synchronized
    private fun getLineData(chartDataList: Vector<Float>, minValue: Float?): LineData {
        val dataSets: MutableList<ILineDataSet> =
            ArrayList()
        val entries: MutableList<Entry> =
            ArrayList()
        val chartDataList1 = Vector<Float>()
        chartDataList1.addAll(chartDataList)
        if (chartDataList1.isNotEmpty()) {
            var max = chartDataList1.first()
            var min = chartDataList1.first()
            for (index in chartDataList1.indices) {
                if (minValue != null) {
                    entries.add(Entry(index.toFloat(), chartDataList1[index] - minValue))
                } else {
                    entries.add(Entry(index.toFloat(), chartDataList1[index]))
                }
                min = min(min, chartDataList1[index])
                max = max(max, chartDataList1[index])
            }
            val dataSet = LineDataSet(entries, "")
            initDataSet(dataSet)
            dataSets.add(dataSet)
            this.numMax = max
            this.numData = chartDataList1.last()
            this.numMin = min
        }
        return LineData(dataSets)
    }

    @Volatile
    var numMax: Float? = null

    @Volatile
    var numData: Float? = null

    @Volatile
    var numMin: Float? = null

    open fun updateFzValue() {
        val df1 = DecimalFormat("0.00")
        numMax?.let {
            num1.text = df1.format(it)
        }
        numData?.let {
            num2.text = df1.format(numData)
        }
        numMin?.let {
            num3.text = df1.format(it)
        }
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

    fun updateData(list: Vector<Float>?, minValue: Float?) {
        if (list != null) {
            lineChart.data = getLineData(list, minValue)
            lineChart.invalidate()
        } else {
            num1.text = null
            num2.text = null
            num3.text = null
            lineChart.clear()
            lineChart.invalidate()
        }
    }
}