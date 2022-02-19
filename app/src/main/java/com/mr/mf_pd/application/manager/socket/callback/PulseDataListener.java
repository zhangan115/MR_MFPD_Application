package com.mr.mf_pd.application.manager.socket.callback;

public interface PulseDataListener {

    void onRead(byte[] sourceBytes);
}
