package com.mr.mf_pd.application.blueTooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;



/**
 * 服务端监听和连接线程，只连接一个设备
 */
public class BtServer extends BtBase {
    private static final String TAG = BtServer.class.getSimpleName();
    private BluetoothServerSocket mSSocket;

    public BtServer(Listener listener) {
        super(listener);
        listen();
    }

    /**
     * 监听客户端发起的连接
     */
    public void listen() {
        try {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            mSSocket = adapter.listenUsingInsecureRfcommWithServiceRecord(TAG, SPP_UUID); //明文传输(不安全)，无需配对
            // 开启子线程
            Util.EXECUTOR.execute(() -> {
                try {
                    BluetoothSocket socket = mSSocket.accept(); // 监听连接
                    mSSocket.close(); // 关闭监听，只连接一个设备
                    loopRead(socket); // 循环读取
                } catch (Throwable e) {
                    notifyUI(Listener.ERROR, e.getMessage());
                    close();
                }
            });
        } catch (Throwable e) {
            notifyUI(Listener.ERROR, e.getMessage());
            close();
        }
    }

    @Override
    public void close() {
        super.close();
        try {
            if (mSSocket != null)
                mSSocket.close();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}