package com.mr.mf_pd.application.utils;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import java.math.BigInteger;

public class NumberUtils {

    public static boolean isNumber(String str) {
        if (TextUtils.isEmpty(str)) return false;
        return str.matches("-?[0-9]+.？[0-9]*");
    }

    @Nullable
    public static Float getFloat(String str) {
        Float value = null;
        try {
            value = Float.parseFloat(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    /**
     * 将 4字节的16进制字符串，转换为32位带符号的十进制浮点型
     *
     * @param str 4字节 16进制字符
     * @return float
     */
    public static float hexToFloat(String str) {
        return Float.intBitsToFloat(new BigInteger(str, 16).intValue());
    }

    /**
     * 将带符号的32位浮点数装换为16进制
     *
     * @param value float值
     * @return 16进制数据
     */
    public static String floatToHexString(Float value) {
        String s = Integer.toHexString(Float.floatToIntBits(value));
        if (s.length() == 8) {
            String s1 = s.substring(0, 4);
            String s2 = s.substring(4, 8);
            return s1 + "," + s2;
        }
        return "0000,0000";
    }

    /**
     * 修改到合适的最大值
     *
     * @param max  最大值
     * @param step 调整参数
     * @return 修改后的最大值
     */
    public static int changeMaxValue(int max, int step) {
        return ((max / step) + 1) * step;
    }

    /**
     * 修改到合适的最小值
     *
     * @param min  最小值
     * @param step 调整参数
     * @return 修改后的最小值
     */
    public static int changeMinValue(int min, int step) {
        return ((min / step) - 1) * step;
    }

}
