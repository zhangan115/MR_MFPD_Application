package com.mr.mf_pd.application.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.github.mikephil.charting.charts.ScatterChart;
import com.mr.mf_pd.application.R;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class MRScatterChartLayout extends LinearLayout {

    private ScatterChart mScatterChart;
    private Map<Float, Map<Integer, Vector<Float>>> mChartData = new HashMap<>();

    public MRScatterChartLayout(Context context) {
        super(context,null);
    }

    public MRScatterChartLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs,0);
    }

    public MRScatterChartLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    protected void init(Context context){
        inflate(context, R.layout.layout_charts,this);
        mScatterChart = findViewById(R.id.scatterChart);
        initChart(mScatterChart,context);

    }

    public synchronized void cleanData(){
        mChartData.clear();
    }

    public synchronized void updateData(ConcurrentHashMap<Integer, ConcurrentHashMap<Float, Integer>> map){
        for (Integer key1:map.keySet()){
            ConcurrentHashMap<Float, Integer> map2 = map.get(key1);
            if (map2 != null) {
                for (Float value: map2.keySet()){
                    Integer count = map2.get(value);

                }
            }
        }
        mScatterChart.invalidate();
    }

    private void initChart(ScatterChart lineChart, Context content) {
        lineChart.clear();
        lineChart.getDescription().setText(null);
        lineChart.setNoDataText("");
        lineChart.setAlpha(1f);
        lineChart.setMaxVisibleValueCount(1000);
        lineChart.setMinOffset(0f);
        lineChart.setTouchEnabled(false);
        lineChart.setDragEnabled(false);
        lineChart.setScaleEnabled(false);
        lineChart.setPinchZoom(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.getAxisRight().setEnabled(false);

        lineChart.getXAxis().setEnabled(true);
        lineChart.getXAxis().setTextColor(content.getResources().getColor(R.color.text_black, null));
        lineChart.getXAxis().setAxisLineColor(content.getResources().getColor(R.color.chart_xy_color, null));
        lineChart.getXAxis().setGridColor(content.getResources().getColor(R.color.chart_line_color, null));

        lineChart.getAxisLeft().setDrawZeroLine(false);
        lineChart.getAxisLeft().setDrawGridLines(true);
        lineChart.getAxisLeft().setEnabled(true);
        lineChart.getAxisLeft().setTextColor(content.getResources().getColor(R.color.text_black, null));
        lineChart.getAxisLeft().setAxisLineColor(content.getResources().getColor(R.color.chart_xy_color, null));
        lineChart.getAxisLeft().setGridColor(content.getResources().getColor(R.color.chart_line_color, null));
    }

}
