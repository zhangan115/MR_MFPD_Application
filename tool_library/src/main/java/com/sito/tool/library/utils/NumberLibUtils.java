package com.sito.tool.library.utils;


import java.math.BigInteger;
import java.util.ArrayList;

public class NumberLibUtils {

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

    public static short[] CheckRegValues(String writeValue, int radix) {
        short[] mRegValues = null;
        try {
            String[] split = writeValue.trim().split(",");
            ArrayList<Integer> result = new ArrayList<>();
            for (String s : split) {
                result.add(Integer.parseInt(s.trim(), radix));
            }
            short[] values = new short[result.size()];
            for (int i = 0; i < values.length; i++) {
                int v = result.get(i);
                if (v >= 0 && v <= 0xffff) {
                    values[i] = (short) v;
                } else {
                    throw new RuntimeException();
                }
            }
            if (values.length > 0) {
                mRegValues = values;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mRegValues;
    }
}
