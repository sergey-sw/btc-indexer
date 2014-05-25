package com.ssau.btc.model;

import com.ssau.btc.sys.Config;

import java.io.Serializable;
import java.util.Random;

/**
 * Author: Sergey42
 * Date: 18.05.14 17:37
 */
public class RBFLayer implements Serializable {

    private static final long serialVersionUID = -2620845044706356618L;
    public int size;
    public int dataLength;
    public double[] fuzzyInputs;
    public double[] fuzzyOutputs;
    public double[] fuzzyCenters;
    /* Dimension M - input data count, N - input layer count */
    public double[][] fuzzyBelongs;
    public double[] nData;

    protected transient NetworkMediator networkMediator;
    private Random random = new Random();

    protected void init(NetworkMediator mediator) {
        networkMediator = mediator;
    }

    protected double[] calcRBFOutput(double[] values) {
        // кэшируем расстояния
        double[] belongsCache = new double[size];
        for (int z = 0; z < size; z++) {
            belongsCache[z] = calcBelongToCenter(values, fuzzyCenters[z]);
        }

        // Выход нечеткого слоя
        for (int i = 0; i < size; i++) {
            double top = belongsCache[i];
            top *= top;
            double sum = 0.0;
            for (int k = 0; k < size; k++) {
                double tDistance = belongsCache[k];
                sum += top / (tDistance * tDistance);
            }
            fuzzyOutputs[i] = 1 / sum;
        }
        return fuzzyOutputs;
    }

    protected void teach() {
        fuzzyInitBelongs();
        boolean errorIsOk = false;
        int iterationNumber = 0;
        double errorBarrier = Config.CMEANS_ERROR_BARRIER;
        int iterationsMaxNumber = Config.CMEANS_MAX_ITERATIONS;

        while (!errorIsOk && iterationNumber < iterationsMaxNumber) {
            // Расчет центров
            for (int j = 0; j < size; j++) {
                double tempUpSum = 0.0;
                double tempDownSum = 0.0;

                // Цикл по входной выборке
                double belongSquare;
                for (int i = 0; i < dataLength; i++) {
                    belongSquare = fuzzyBelongs[i][j] * fuzzyBelongs[i][j];
                    tempUpSum += belongSquare * nData[i];
                    tempDownSum += belongSquare;
                }

                fuzzyCenters[j] = tempUpSum / tempDownSum;
            }

            double error = 0.0;
            double temp1;
            double temp2;

            double distance;
            for (int f = 0; f < size; f++) {
                for (int i = 0; i < dataLength; i++) {
                    temp1 = fuzzyBelongs[i][f];
                    distance = fuzzyCenters[f] - nData[i];
                    temp2 = distance * distance;
                    error += temp1 * temp1 * temp2 * temp2;
                }
            }

            // Если ошибка мала, заканчиваем
            if (error < errorBarrier) {
                errorIsOk = true;
                continue;
            }

            // Пересчет коэф-ов принадлежности
            for (int f = 0; f < size; f++) {
                for (int i = 0; i < dataLength; i++) {
                    // еще одна сумма
                    double sum = 0.0;
                    distance = fuzzyCenters[f] - nData[i];
                    double tmp1 = distance * distance * distance * distance;
                    double tmp2;

                    for (int k = 0; k < size; k++) {
                        distance = fuzzyCenters[k] - nData[i];
                        tmp2 = distance * distance * distance * distance;
                        sum += tmp1 / tmp2;
                    }

                    fuzzyBelongs[i][f] = 1 / sum;
                }
            }
            iterationNumber++;
        }
    }

    private void fuzzyInitBelongs() {
        fuzzyBelongs = new double[dataLength][];
        for (int i = 0; i < fuzzyBelongs.length; i++) {
            fuzzyBelongs[i] = new double[size];
        }

        for (int f = 0; f < fuzzyBelongs.length; f++) {
            boolean successInit = false;
            while (!successInit) {
                double tempSum = 0.0;
                for (int i = 0; i < size - 1; i++) {
                    tempSum += fuzzyBelongs[f][i] = random.nextDouble() * 2 / size;
                }

                if (tempSum >= 1) continue;

                fuzzyBelongs[f][size - 1] = (1 - tempSum);
                successInit = true;
            }
        }
    }

    private static double calcBelongToCenter(double[] inputs, double center) {
        double sum = 0.0;
        for (double input : inputs) {
            sum += (center - input) * (center - input);
        }
        return sum;
    }
}
