package com.ssau.btc.utils;

/**
 * Author: Sergey42
 * Date: 25.05.14 14:31
 */
public class MultiDimensionHelper {

    public static double[] mixSingle(double[] data1, int startPos, int size, double[] data2, int startPos2, double[] result) {
        double single = average(data2, startPos2, size);
        System.arraycopy(data1, startPos, result, 0, size);
        result[size] = single;
        return result;
    }

    private static double average(double[] array, int start, int size) {
        double sum = .0;
        for (int i = 0; i < size; i++) {
            sum += array[start + i];
        }
        return sum / size;
    }

    public static double[] approxSecondDimension(double[] approxNData2, double[] nData2, int window) {
        System.arraycopy(nData2, nData2.length - window, approxNData2, 0, window);
        for (int i = window; i < approxNData2.length; i++) {
            double diff = approxNData2[i - 1] - approxNData2[i - 2];
            approxNData2[i] = approxNData2[i - 1] + diff;
        }
        return approxNData2;
    }
}
