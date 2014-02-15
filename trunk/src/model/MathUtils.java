package model;

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
}
