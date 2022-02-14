package com.sito.tool.library

import com.sito.tool.library.utils.ByteLibUtil
import com.sito.tool.library.utils.NumberLibUtils
import org.junit.Assert.*
import org.junit.Test
import java.util.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val value = NumberLibUtils.floatToHexString(20.0f)
        System.out.println(Integer.toHexString(java.lang.Float.floatToIntBits(3.1f)))
        System.out.println(value)
        System.out.println(
            Arrays.toString(
                ByteLibUtil.hexStr2bytes(
                    NumberLibUtils.floatToHexString(
                        20.0f
                    )
                )
            )
        )

        val array = ByteLibUtil.intToByteArray(100)
//        System.out.println(ByteLibUtil.getIntHeightByte(array.first()))
//        System.out.println(ByteLibUtil.getIntLowByte(array.last()))
        System.out.println(ByteLibUtil.bytes2HexStr(array))
        System.out.println(Arrays.toString(array))
        assertEquals(4, 2 + 2)


    }
}
