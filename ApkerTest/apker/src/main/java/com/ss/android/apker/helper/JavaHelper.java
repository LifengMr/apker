package com.ss.android.apker.helper;

import android.text.TextUtils;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * Created by chenlifeng on 16/5/25.
 */
public class JavaHelper {

    /**
     * 字符串转整型值
     *
     * @param str
     */
    public static int stoi(String str) {
        int value = 0;
        if (!TextUtils.isEmpty(str)) {
            try {
                value = Integer.parseInt(str);
            } catch (NumberFormatException e) {
            }
        }
        return value;
    }

    /**
     * 字符串转整型值
     *
     * @param str
     * @param defaultValue 默认值
     * @return
     */
    public static int stoi(String str, final int defaultValue) {
        int value = defaultValue;
        if (!TextUtils.isEmpty(str)) {
            try {
                value = Integer.parseInt(str);
            } catch (NumberFormatException e) {
            }
        }
        return value;
    }

    /**
     * string to float
     *
     * @param str
     * @return
     */
    public static float stof(String str) {
        float value = 0.0f;
        if (!TextUtils.isEmpty(str)) {
            try {
                value = Float.parseFloat(str);
            } catch (NumberFormatException e) {
            }
        }
        return value;
    }

    /**
     * string to float
     *
     * @param str
     * @param defaultValue 默认值
     * @return
     */
    public static float stof(String str, float defaultValue) {
        float value = 0.0f;
        if (!TextUtils.isEmpty(str)) {
            try {
                value = Float.parseFloat(str);
            } catch (NumberFormatException e) {
            }
        }
        return value;
    }

    /**
     * string to double
     *
     * @param str
     * @return
     */
    public static double stod(String str) {
        double value = 0.0;
        if (!TextUtils.isEmpty(str)) {
            try {
                value = Double.parseDouble(str);
            } catch (NumberFormatException e) {
            }
        }
        return value;
    }

    /**
     * string to double
     *
     * @param str
     * @param defaultValue 默认值
     * @return
     */
    public static double stod(String str, final double defaultValue) {
        double value = defaultValue;
        if (!TextUtils.isEmpty(str)) {
            try {
                value = Double.parseDouble(str);
            } catch (NumberFormatException e) {
            }
        }
        return value;
    }

    /**
     * string to long
     *
     * @param str
     * @return
     */
    public static long stol(String str) {
        long value = 0;
        if (!TextUtils.isEmpty(str)) {
            try {
                value = Long.parseLong(str);
            } catch (NumberFormatException e) {
            }
        }
        return value;
    }

    /**
     * string to long
     *
     * @param defaultValue
     * @return
     */
    public static long stol(String str, final long defaultValue) {
        long value = defaultValue;
        if (TextUtils.isEmpty(str)) {
            try {
                value = Long.parseLong(str);
            } catch (NumberFormatException e) {
            }
        }
        return value;
    }

    /**
     * list是否为空
     *
     * @param list
     * @return true if list is null or zero size
     */
    public static <T> boolean isListEmpty(List<T> list) {
        return list == null || list.isEmpty();
    }

    /**
     * 队列是否为空
     * @param queue
     * @param <T>
     * @return
     */
    public static <T> boolean isQueueEmpty(Queue<T> queue) {
        return queue == null || queue.isEmpty();
    }

    /**
     * 从list中获取数据
     *
     * @param list
     * @param index 索引
     * @return
     */
    public static <T> T getElementFromList(List<T> list, int index) {
        if (isListEmpty(list)) {
            return null;
        }

        if (index < 0 || index >= list.size()) {
            return null;
        }

        return list.get(index);
    }

    /**
     * array是否为空
     *
     * @param array
     * @return true if array is null or zero size
     */
    public static <T> boolean isArrayEmpty(T[] array) {
        return array == null || array.length == 0;
    }

    /**
     * 从array中获取数据
     *
     * @param array
     * @param index 索引
     * @return
     */
    public static <T> T getElementFromArray(T[] array, int index) {
        if (isArrayEmpty(array)) {
            return null;
        }

        if (index < 0 || index >= array.length) {
            return null;
        }

        return array[index];
    }

    public static int getElementFromIntArray(int[] array, int index) {
        if (array == null || array.length == 0) {
            return 0;
        }

        if (index < 0 || index >= array.length) {
            return 0;
        }

        return array[index];
    }

    public static float getElementFromFloatArray(float[] array, int index) {
        if (array == null || array.length == 0) {
            return 0;
        }

        if (index < 0 || index >= array.length) {
            return 0;
        }

        return array[index];
    }

    /**
     * map是否为空
     *
     * @param map
     * @return true if map is null or zero size
     */
    public static boolean isMapEmpty(Map<? extends Object, ? extends Object> map) {
        return map == null || map.isEmpty();
    }

    /**
     * map是否包含key
     *
     * @param map
     * @param key
     * @return
     */
    public static boolean isMapContainsKey(Map<? extends Object, ? extends Object> map, Object key) {
        return key != null && !isMapEmpty(map) && map.containsKey(key);
    }

    /**
     * map是否包含key
     *
     * @param map
     * @param key
     * @return
     */
    public static <T> T getElementFromMap(Map<? extends Object, T> map, Object key) {
        if (isMapContainsKey(map, key)) {
            return map.get(key);
        }

        return null;
    }

    /**
     * set是否为空
     *
     * @param set
     * @return
     */
    public static boolean isSetEmpty(Set<?> set) {
        return set == null | set.isEmpty();
    }

}
