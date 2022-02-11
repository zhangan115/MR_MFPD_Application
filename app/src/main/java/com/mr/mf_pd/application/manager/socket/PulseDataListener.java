package com.mr.mf_pd.application.manager.socket;

public interface PulseDataListener {

    void onRead(byte[] sourceBytes);
}
