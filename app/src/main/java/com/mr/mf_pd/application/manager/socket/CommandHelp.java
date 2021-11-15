package com.mr.mf_pd.application.manager.socket;

import com.mr.mf_pd.application.model.SettingBean;
import com.mr.mf_pd.application.utils.ByteUtil;

import java.util.Calendar;

/**
 * 命令类
 *
 * @author anson
 */
public class CommandHelp {

    /**
     * 获取对时命令
     *
     * @return 对时命令
     */
    public static byte[] getTimeCommand() {
        byte year = (byte) (Calendar.getInstance().get(Calendar.YEAR) - 2000);
        byte month = (byte) (Calendar.getInstance().get(Calendar.MONTH) + 1);
        byte day = (byte) Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        byte hour = (byte) Calendar.getInstance().get(Calendar.HOUR);
        byte minute = (byte) Calendar.getInstance().get(Calendar.MINUTE);
        int second = Calendar.getInstance().get(Calendar.SECOND);
        int millSecond = Calendar.getInstance().get(Calendar.MILLISECOND);
        byte[] secondByte = ByteUtil.intToByteArray(second * 1000 + millSecond);
        byte[] bytes = new byte[]{
                0x01, 0x06, year, month, day, hour, minute, secondByte[0], secondByte[1]
        };
        byte[] crcByte = ByteUtil.hexStr2bytes(ByteUtil.getCRC(bytes));
        byte[] data = new byte[crcByte.length + bytes.length];
        System.arraycopy(bytes, 0, data, 0, bytes.length);
        System.arraycopy(crcByte, 0, data, bytes.length, crcByte.length);
        return data;
    }

    /**
     * 切换通道
     *
     * @param passageway 通道
     * @return 切换通道命令
     */
    public static byte[] switchPassageway(int passageway) {
        byte b = (byte) passageway;
        byte[] bytes = new byte[]{
                0x01, 0x07, 0x00, 0x00, 0x00, b
        };
        byte[] crcByte = ByteUtil.hexStr2bytes(ByteUtil.getCRC(bytes));
        byte[] data = new byte[crcByte.length + bytes.length];
        System.arraycopy(bytes, 0, data, 0, bytes.length);
        System.arraycopy(crcByte, 0, data, bytes.length, crcByte.length);
        return data;
    }

    /**
     * 读取设置的定值
     *
     * @param passageway 通道
     * @return 定值数组
     */
    public static byte[] readSettingValue(int passageway) {
        // TODO: 11/15/21 定值命令需要修改
        byte b = (byte) passageway;
        byte[] bytes = new byte[]{
                0x01, 0x04, 0x00, b, 0x00, 0x07
        };
        byte[] crcByte = ByteUtil.hexStr2bytes(ByteUtil.getCRC(bytes));
        byte[] data = new byte[crcByte.length + bytes.length];
        System.arraycopy(bytes, 0, data, 0, bytes.length);
        System.arraycopy(crcByte, 0, data, bytes.length, crcByte.length);
        return data;
    }

    /**
     * 写设置的定值
     *
     * @param passageway 通道
     * @return 读取定值命令
     */
    public static byte[] setSettingValue(int passageway, SettingBean settingBean) {
        // TODO: 11/15/21 定值命令需要修改
        byte b = (byte) passageway;
        byte[] bytes = new byte[]{
                0x01, 0x0c, 0x00, b, 0x00, 0x00
        };
        byte[] crcByte = ByteUtil.hexStr2bytes(ByteUtil.getCRC(bytes));
        byte[] data = new byte[crcByte.length + bytes.length];
        System.arraycopy(bytes, 0, data, 0, bytes.length);
        System.arraycopy(crcByte, 0, data, bytes.length, crcByte.length);
        return data;
    }


}
