package com.mr.mf_pd.application.manager.socket;

public abstract class ReadListener {

    public int filter;

    public ReadListener(int filter) {
        this.filter = filter;
    }

    public abstract void onRead(byte[] sourceBytes);
}
