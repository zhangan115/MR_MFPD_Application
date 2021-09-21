package com.mr.mf_pd.application.manager;

public abstract class ReadListener {

    public int filter = 1;

    public ReadListener(int filter) {
        this.filter = filter;
    }

    public abstract void onRead( byte[] bytes);
}
