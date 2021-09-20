package com.mr.mf_pd.application.manager;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class SocketManager {

    private final static String host = "192.168.0.55";//请求地址
    private final static int port = 502;//端口

    private InputStream inputStream;//输入流
    private OutputStream outputStream;//输出流

    private static Socket socket;

    private static int deviceNo;

    private static boolean isConnected;//是否连接

    private static SocketManager instance;

    private List<ReadListener> readListeners;

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
        if (socket == null) {
            linkSocket();
        }
    }

    public boolean isConnected() {
        return isConnected;
    }

    /**
     * socket 连接
     */
    private void linkSocket() {
        new Thread(() -> {
            try {
                socket = new Socket(host, port);
                socket.setKeepAlive(true);
                isConnected = socket.isConnected();
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
                byte[] buf = new byte[1024 * 2];
                int size = 0;
                while ((size = inputStream.read(buf)) != -1) {
                    if (!readListeners.isEmpty()) {
                        for (ReadListener listener : readListeners) {
                            listener.onRead(buf);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                isConnected = false;
                socket = null;
            }finally {
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
        }).start();
    }

    public void addReadListener(ReadListener listener) {
        if (readListeners == null) {
            readListeners = new ArrayList<>();
        }
        readListeners.add(listener);
    }

    public List<ReadListener> getReadListeners() {
        return readListeners;
    }

    public void setReadListeners(List<ReadListener> readListeners) {
        this.readListeners = readListeners;
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
