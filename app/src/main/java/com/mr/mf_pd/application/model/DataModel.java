package com.mr.mf_pd.application.model;

import com.mr.mf_pd.application.R;
import com.mr.mf_pd.application.app.MRApplication;

public class DataModel {

    private final int position;
    private final float value;
    private int count;
    private int color;

    public DataModel(float value, int position) {
        this.value = value;
        this.position = position;
    }

    public boolean updateCount(float value, int position) {
        if (this.value != value || this.position != position) {
            return false;
        }
        this.count++;
        if (this.count < 10) {
            this.count = MRApplication.instance.getColor(R.color.prps_blue);
        } else if (this.count > 10 && this.count <= 20) {
            this.count = MRApplication.instance.getColor(R.color.prps_green);
        } else if (this.count > 20 && this.count <= 30) {
            this.count = MRApplication.instance.getColor(R.color.prps_yellow);
        } else if (this.count > 30 && this.count <= 40) {
            this.count = MRApplication.instance.getColor(R.color.prps_orange);
        } else {
            this.count = MRApplication.instance.getColor(R.color.prps_red);
        }
        return true;
    }

    public int getColor() {
        return color;
    }
}
