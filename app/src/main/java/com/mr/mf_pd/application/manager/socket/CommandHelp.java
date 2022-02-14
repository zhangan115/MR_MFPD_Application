package com.mr.mf_pd.application.manager.socket;

import com.mr.mf_pd.application.model.SettingBean;
import com.mr.mf_pd.application.utils.ByteUtil;
import com.sito.tool.library.utils.ByteLibUtil;
import com.sito.tool.library.utils.NumberLibUtils;

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
     * 关闭当前打开的通道
     *
     * @return 命令
     */
    public static byte[] closePassageway() {
        byte[] bytes = new byte[]{
                0x01, 0x07, 0x00, 0x00, 0x00, (byte) 0xff
        };
        byte[] crcByte = ByteUtil.hexStr2bytes(ByteUtil.getCRC(bytes));
        byte[] data = new byte[crcByte.length + bytes.length];
        System.arraycopy(bytes, 0, data, 0, bytes.length);
        System.arraycopy(crcByte, 0, data, bytes.length, crcByte.length);
        return data;
    }

    /**
     * 读取全遥测数据
     *
     * @param passageway 通道号
     * @return 命令
     */
    public static byte[] readYcValue(int passageway) {
        byte b = (byte) passageway;
        byte[] bytes = new byte[]{
                0x01, 0x03, 0x00, b, 0x00, 0x07
        };
        byte[] crcByte = ByteUtil.hexStr2bytes(ByteUtil.getCRC(bytes));
        byte[] data = new byte[crcByte.length + bytes.length];
        System.arraycopy(bytes, 0, data, 0, bytes.length);
        System.arraycopy(crcByte, 0, data, bytes.length, crcByte.length);
        return data;
    }

    /***
     * 读取固定值
     * @param passageway 通道
     * @param length 长度
     * @return 命令
     */
    public static byte[] readSettingValue(int passageway, int length) {
        byte b = (byte) passageway;
        byte[] l = new byte[2];
        ByteUtil.putShort(l, (short) length, 0);
        byte[] bytes = new byte[]{
                0x01, 0x04, 0x00, b, l[0], l[1]
        };
        byte[] crcByte = ByteUtil.hexStr2bytes(ByteUtil.getCRC(bytes));
        byte[] data = new byte[crcByte.length + bytes.length];
        System.arraycopy(bytes, 0, data, 0, bytes.length);
        System.arraycopy(crcByte, 0, data, bytes.length, crcByte.length);
        return data;
    }


    /***
     * 写设置的定值
     * @param passageway 通道
     * @param valuePosition 数值位置
     * @param value 值
     * @return 命令
     */
    public static byte[] writeSettingValue(int passageway, int valuePosition, Float value) {
        byte b = (byte) passageway;

        byte[] valueByte = ByteLibUtil.hexStr2bytes(NumberLibUtils.floatToHexString(value));

        byte[] bytes = new byte[]{
                0x01, 0x10, b, (byte) valuePosition, valueByte[0], valueByte[1], valueByte[2], valueByte[3]
        };
        byte[] crcByte = ByteUtil.hexStr2bytes(ByteUtil.getCRC(bytes));
        byte[] data = new byte[crcByte.length + bytes.length];
        System.arraycopy(bytes, 0, data, 0, bytes.length);
        System.arraycopy(crcByte, 0, data, bytes.length, crcByte.length);
        return data;
    }

    /**
     * 读取脉冲数据
     *
     * @param passageway 通道号
     * @return 读取脉冲数据命令
     */
    public static byte[] readPulseData(int passageway) {
        byte b = (byte) passageway;
        byte[] bytes = new byte[]{
                0x01, 0x13, 0x00, b, 0x00, (byte) 0xff
        };
        byte[] crcByte = ByteUtil.hexStr2bytes(ByteUtil.getCRC(bytes));
        byte[] data = new byte[crcByte.length + bytes.length];
        System.arraycopy(bytes, 0, data, 0, bytes.length);
        System.arraycopy(crcByte, 0, data, bytes.length, crcByte.length);
        return data;
    }

    /**
     * 读取脉冲数据
     *
     * @param passageway 通道号
     * @return 读取脉冲数据命令
     */
    public static byte[] stopReadPulseData(int passageway) {
        byte b = (byte) passageway;
        byte[] bytes = new byte[]{
                0x01, 0x13, 0x00, b, 0x00, 0x00
        };
        byte[] crcByte = ByteUtil.hexStr2bytes(ByteUtil.getCRC(bytes));
        byte[] data = new byte[crcByte.length + bytes.length];
        System.arraycopy(bytes, 0, data, 0, bytes.length);
        System.arraycopy(crcByte, 0, data, bytes.length, crcByte.length);
        return data;
    }


}
