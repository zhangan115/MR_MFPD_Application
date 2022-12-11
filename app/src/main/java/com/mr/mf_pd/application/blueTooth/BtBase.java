package com.mr.mf_pd.application.blueTooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Environment;


import com.mr.mf_pd.application.app.MRApplication;

import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.UUID;

/**
 * 客户端和服务端的基类，用于管理socket长连接
 */
public class BtBase {

    static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/blueToothPhoto/";
    public static final int FLAG_MSG = 0;  //消息标记
    public static final int FLAG_FILE = 1; //文件标记
    public static final int FLAG_RESPONSE = 2;  //响应标记

    private BluetoothSocket mSocket;
    private DataOutputStream mOut;
    private Listener mListener;
    private boolean isRead;
    private boolean isSending;

    BtBase(Listener listener) {
        mListener = listener;
    }

    /**
     * 循环读取对方数据(若没有数据，则阻塞等待)
     */
    void loopRead(BluetoothSocket socket) {
        mSocket = socket;
        try {
            if (!mSocket.isConnected())
                mSocket.connect();
            notifyUI(Listener.CONNECTED, mSocket.getRemoteDevice());
            mOut = new DataOutputStream(mSocket.getOutputStream());
            DataInputStream in = new DataInputStream(mSocket.getInputStream());
            isRead = true;
            while (isRead) { //死循环读取
                switch (in.readInt()) {
                    case FLAG_MSG: //读取短消息
                        String msg = in.readUTF();
                        notifyUI(Listener.RESULT, msg);
                        break;
                    case FLAG_FILE: //读取文件
                        Util.mkdirs(FILE_PATH);
                        String fileName = in.readUTF(); //文件名
                        long fileLen = in.readLong(); //文件长度
                        // 读取文件内容
                        long len = 0;
                        int r;
                        byte[] b = new byte[4 * 1024];
                        FileOutputStream out = new FileOutputStream(FILE_PATH + fileName);
                        notifyUI(Listener.MSG, "正在接收文件(" + fileName + "),请稍后...");
                        while ((r = in.read(b)) != -1) {
                            out.write(b, 0, r);
                            len += r;
                            if (len >= fileLen)
                                break;
                        }
                        notifyUI(Listener.MSG, fileName + "文件接收完成");
                        JSONObject jsonFile = new JSONObject();
                        jsonFile.put("errorCode", 0);
                        jsonFile.put("fileName", fileName);
                        jsonFile.put("type", FLAG_FILE);
                        sendResponse(jsonFile.toString());
                        break;
                    case FLAG_RESPONSE:
                        String responseMsg = in.readUTF();
                        //处理手机端发送的响应
                        notifyUI(Listener.MSG, "接收响应的消息：" + responseMsg);
                        break;
                }
            }
        } catch (Throwable e) {
            notifyUI(Listener.ERROR, e.getMessage());
            close();
        }
    }

    /**
     * 发送短消息
     */
    public void sendMsg(String msg) {
        if (checkSend()) return;
        isSending = true;
        try {
            mOut.writeInt(FLAG_MSG); //消息标记
            mOut.writeUTF(msg);
            mOut.flush();
            notifyUI(Listener.MSG, "发送短消息：" + msg);
        } catch (Throwable e) {
            notifyUI(Listener.ERROR, e.getMessage());
            close();
        }
        isSending = false;
    }

    /**
     * 发送响应信息
     */
    public void sendResponse(String msg) {
        if (checkSend()) return;
        isSending = true;
        try {
            mOut.writeInt(FLAG_RESPONSE); //响应标记
            mOut.writeUTF(msg);
            mOut.flush();
//            notifyUI(Listener.MSG, "发送响应的消息：" + msg);
        } catch (Throwable e) {
            notifyUI(Listener.ERROR, e.getMessage());
            close();
        }
        isSending = false;
    }

    /**
     * 发送文件
     */
    public void sendFile(final String filePath) {
        if (checkSend()) return;
        isSending = true;
        Util.EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    FileInputStream in = new FileInputStream(filePath);
                    File file = new File(filePath);
                    mOut.writeInt(FLAG_FILE); //文件标记
                    mOut.writeUTF(file.getName()); //文件名
                    mOut.writeLong(file.length()); //文件长度
                    int r;
                    byte[] b = new byte[4 * 1024];
                    notifyUI(Listener.MSG, "正在发送文件(" + filePath + "),请稍后...");
                    while ((r = in.read(b)) != -1)
                        mOut.write(b, 0, r);
                    mOut.flush();
                    notifyUI(Listener.MSG, "文件发送完成.");
                } catch (Throwable e) {
                    notifyUI(Listener.ERROR, e.getMessage());
                    close();
                }
                isSending = false;
            }
        });
    }

    /**
     * 释放监听引用(例如释放对Activity引用，避免内存泄漏)
     */
    public void unListener() {
        mListener = null;
    }

    /**
     * 关闭Socket连接
     */
    public void close() {
        try {
            isRead = false;
            if (mSocket != null)
                mSocket.close();
            notifyUI(Listener.DISCONNECTED, null);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * 当前设备与指定设备是否连接
     */
    public boolean isConnected(BluetoothDevice dev) {
        boolean connected = (mSocket != null && mSocket.isConnected());
        if (dev == null)
            return connected;
        return connected && mSocket.getRemoteDevice().equals(dev);
    }

    private boolean checkSend() {
        if (isSending) {

            return true;
        }
        return false;
    }

    public void notifyUI(final int state, final Object obj) {
        MRApplication.Companion.runUi(() -> {
            try {
                if (mListener != null){
                    mListener.socketNotify(state, obj);
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
    }

    public interface Listener {
        int DISCONNECTED = 0;
        int CONNECTED = 1;
        int MSG = 2;
        int ERROR = 3;
        int RESULT = 4;

        void socketNotify(int state, Object obj);
    }
}
