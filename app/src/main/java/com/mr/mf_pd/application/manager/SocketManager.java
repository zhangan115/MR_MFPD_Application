package com.mr.mf_pd.application.manager;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SocketManager {

    private final static String host = "172.16.40.1";//请求地址
    private final static int port = 502;//端口

    private InputStream inputStream;//输入流
    private OutputStream outputStream;//输出流

    private static Socket socket;

    private static final int DEVICE_NO = 1;

    private static boolean isConnected;//是否连接

    private static SocketManager instance;

    private List<ReadListener> readListeners;

    private List<LinkStateListener> linkStateListeners;

    //执行请求任务的线程池
    private ExecutorService mRequestExecutor = null;
    private Future future;

    /**
     * 单例模式
     *
     * @return 唯一Socket
     */
    public static synchronized SocketManager getInstance() {
        if (instance == null) {
            instance = new SocketManager();
        }
        return instance;
    }

    private SocketManager() {
        mRequestExecutor = Executors.newSingleThreadExecutor();
    }

    public boolean isConnected() {
        return isConnected;
    }

    private final Runnable requestRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                socket = new Socket(host, port);
                socket.setKeepAlive(true);
                isConnected = socket.isConnected();
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
                byte[] buf = new byte[1024 * 2];
                int size;
                while ((size = inputStream.read(buf)) != -1) {
                    if (!readListeners.isEmpty()) {
                        for (ReadListener listener : readListeners) {
                            byte[] newBuf = new byte[size];
                            System.arraycopy(buf, 0, newBuf, 0, size);
                            Log.d("za", Arrays.toString(newBuf));
                            listener.onRead(newBuf);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                isConnected = false;
                socket = null;
            } finally {
                try {
                    isConnected = false;
                    if (socket != null) {
                        socket.close();
                    }
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (outputStream != null) {
                        outputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    /**
     * 释放请求线程
     */
    public void releaseRequest() {
        destroy();
        if (future != null && future.isCancelled()) {
            future.cancel(true);
        }
        if (mRequestExecutor != null && !mRequestExecutor.isShutdown()) {
            mRequestExecutor.shutdownNow();
            mRequestExecutor = null;
        }
    }

    /**
     * socket 连接
     */
    public void initLink() {
        future = mRequestExecutor.submit(requestRunnable);
    }

    /**
     * 增加状态监控
     *
     * @param listener 状态监控
     */
    public void addLinkStateListeners(LinkStateListener listener) {
        if (linkStateListeners == null) {
            linkStateListeners = new ArrayList<>();
        }
        linkStateListeners.add(listener);
    }

    /**
     * 移除状态监控
     *
     * @param listener 状态监控
     */
    public void removeLinkStateListener(LinkStateListener listener) {
        if (linkStateListeners != null && !linkStateListeners.isEmpty() && listener != null) {
            linkStateListeners.remove(listener);
        }
    }

    /**
     * 增加读取监控
     *
     * @param listener 读取监控
     */
    public void addReadListener(ReadListener listener) {
        if (readListeners == null) {
            readListeners = new ArrayList<>();
        }
        readListeners.add(listener);
    }

    /**
     * 移除读取回调
     *
     * @param listener 读取监控
     */
    public void removeReadListener(ReadListener listener) {
        if (readListeners != null && !readListeners.isEmpty() && listener != null) {
            readListeners.remove(listener);
        }
    }

    /**
     * 销毁socket
     */
    public void destroy() {
        try {
            if (socket != null) {
                socket.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            socket = null;
        }
    }
}
