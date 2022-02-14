package com.mr.mf_pd.application.manager.socket;

import android.util.Log;

import com.google.common.primitives.Bytes;
import com.mr.mf_pd.application.common.Constants;
import com.mr.mf_pd.application.utils.ByteUtil;
import com.sito.tool.library.utils.ByteLibUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class SocketManager {

    private InputStream inputStream;//输入流
    private OutputStream outputStream;//输出流

    private static Socket socket;

    private static final int DEVICE_NO = 1;

    private static boolean isConnected;//是否连接

    private static SocketManager instance;

    private ReadListener readListener;

    private PulseDataListener mPulseDataListener;

    private List<LinkStateListener> linkStateListeners;

    private final Map<Byte, ReceiverCallback> callbackMap = new HashMap<>();
    private final Map<Byte, Observable<byte[]>> sendDataObservableMap = new HashMap<>();
    private final Map<Byte, ObservableEmitter<byte[]>> emitterMap = new HashMap<>();

    //执行请求任务的线程池
    private ExecutorService mRequestExecutor;
    private Future<?> future;

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

    }

    private final Runnable requestRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                InetSocketAddress address = new InetSocketAddress(Constants.host, Constants.port);
                socket = new Socket();
                socket.connect(address, 2000);
                socket.setKeepAlive(true);
                isConnected = socket.isConnected();
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
                for (int i = 0; i < linkStateListeners.size(); i++) {
                    linkStateListeners.get(i).onLinkState(Constants.LINK_SUCCESS);
                }
                byte[] buf = new byte[1024 * 4];
                List<Byte> byteList = new ArrayList<>();
                int size;
                while ((size = inputStream.read(buf)) != -1) {
                    try {
                        byte[] sources = new byte[size];
                        System.arraycopy(buf, 0, sources, 0, size);
                        byteList.addAll(Bytes.asList(sources));
                        dealStickyBytes(byteList);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                socket = null;
                for (int i = 0; i < linkStateListeners.size(); i++) {
                    linkStateListeners.get(i).onLinkState(Constants.LINK_FAIL);
                }
            } finally {
                try {
                    isConnected = false;
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (outputStream != null) {
                        outputStream.close();
                    }
                    if (socket != null) {
                        socket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    /***
     * 处理黏包
     * @param byteList 字节集合
     */
    private void dealStickyBytes(List<Byte> byteList) {
        if (byteList.size() > 3 && byteList.get(0) == 1) {
            //对定长的数据进行单独处理
            if (byteList.get(1) == CommandType.SendTime.getFunCode()) {
                if (byteList.size() >= CommandType.SendTime.getLength()) {
                    byteList.removeAll(handOut(byteList, CommandType.SendTime.getLength()));
                }
            } else if (byteList.get(1) == CommandType.SwitchPassageway.getFunCode()) {
                if (byteList.size() >= CommandType.SwitchPassageway.getLength()) {
                    byteList.removeAll(handOut(byteList, CommandType.SwitchPassageway.getLength()));
                }
            } else if (byteList.get(1) == CommandType.ReadYcData.getFunCode()) {
                int length = byteList.get(2).intValue() * 4 + 5;
                byteList.removeAll(handOut(byteList, length));
            } else if (byteList.get(1) == CommandType.ReadSettingValue.getFunCode()) {
                int length = byteList.get(2).intValue() * 4 + 5;
                byteList.removeAll(handOut(byteList, length));
            } else if (byteList.get(1) == CommandType.WriteValue.getFunCode()) {
                byteList.removeAll(handOut(byteList, CommandType.WriteValue.getLength()));
            } else {
                //byte数组中包含长度
                int length = byteList.get(4).intValue() * 4 + 7;
                byteList.removeAll(handOut(byteList, length));
            }
        }
        if (byteList.size() > 0) {
            dealStickyBytes(byteList);
        }
    }

    private List<Byte> handOut(List<Byte> byteList, int length) {
        List<Byte> list = byteList.subList(0, length);
        byte[] sources = Bytes.toArray(list);
        if (sources[1] == CommandType.RealData.getFunCode()) {//上送实时数据
            if (readListener != null) {
                if (sources[2] == readListener.filter) {
                    readListener.onRead(sources);
                }
            }
        } else if (sources[1] == CommandType.SendPulse.getFunCode()) {//上送原始脉冲数据
            if (mPulseDataListener != null) {
                mPulseDataListener.onRead(sources);
            }
        } else {//其他数据
            if (emitterMap.containsKey(sources[1])) {
                ObservableEmitter<byte[]> emitter = emitterMap.remove(sources[1]);
                if (emitter != null) {
                    emitter.onNext(sources);
                    emitter.onComplete();
                }
            }
        }
        return list;
    }


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
        callbackMap.clear();
    }

    /**
     * socket 连接
     */
    public void initLink() {
        mRequestExecutor = Executors.newSingleThreadExecutor();
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
     * 发送数据
     *
     * @param data     数据
     * @param cmdType  命令类型
     * @param callback 回调
     */
    public synchronized Disposable sendData(byte[] data, CommandType cmdType, ReceiverCallback callback) {
        if (callback != null) {
            this.callbackMap.put(cmdType.getFunCode(), callback);
        }
        return Observable.create((ObservableOnSubscribe<byte[]>)
                emitter -> {
                    try {
                        if (emitterMap.containsKey(cmdType.getFunCode())) {
                            ObservableEmitter<byte[]> e = emitterMap.remove(cmdType.getFunCode());
                            if (!e.isDisposed()) {
                                e.onError(new Throwable("send command repeat"));
                            }
                        }
                        emitterMap.put(cmdType.getFunCode(), emitter);
                        if (outputStream != null && socket != null && !socket.isClosed()) {
                            outputStream.write(data);
                            outputStream.flush();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        emitter.onError(e);
                    }
                })
                .timeout(3, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(bytes -> {
                    if (callback != null) {
                        callback.onReceiver(bytes);
                    }
                }, Throwable::printStackTrace);
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
    public void setReadListener(ReadListener listener) {
        readListener = listener;
    }

    /**
     * 移除读取回调
     */
    public void removeReadListener() {
        readListener = null;
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
            if (linkStateListeners != null) {
                linkStateListeners.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            socket = null;
        }
    }
}
