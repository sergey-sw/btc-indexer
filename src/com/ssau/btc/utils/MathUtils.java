package com.ssau.btc.utils;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Author: Sergey42
 * Date: 14.02.14 22:58
 */
public class MathUtils {

    public static double findMax(double[] array) {
        double max = Double.MIN_VALUE;
        for (double element : array) {
            if (max < element) {
                max = element;
            }
        }
        return max;
    }

    public static double findMin(double[] array) {
        double min = Double.MAX_VALUE;
        for (double element : array) {
            if (min > element) {
                min = element;
            }
        }
        return min;
    }

    public static int randInt(Random rand, int min, int max) {
        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        return rand.nextInt((max - min) + 1) + min;
    }

    public static double sum(double[] array) {
        double res = 0.0;
        for (double d : array) {
            res += d;
        }
        return res;
    }

    public static double average(double[] array) {
        double res = 0.0;
        for (double d : array) {
            res += d;
        }
        return res / array.length;
    }

    public static double normalize(double value, double min, double max) {
        return 2 * (value - 0.5 * (max + min)) / (max - min);
    }

    public static double denormalize(double value, double min, double max) {
        return value * (max - min) / 2 + 0.5 * (max + min);
    }

    public static double[] convertDoubles(List<Double> doubles) {
        double[] ret = new double[doubles.size()];
        Iterator<Double> iterator = doubles.iterator();
        for (int i = 0; i < ret.length; i++) {
            ret[i] = iterator.next();
        }
        return ret;
    }
}
