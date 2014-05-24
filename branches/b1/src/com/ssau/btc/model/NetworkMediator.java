package com.ssau.btc.model;

/**
 * Author: Sergey42
 * Date: 18.05.14 17:23
 */
public interface NetworkMediator {

    double[] calcNetOutput(double[] inputs);

    double correctWeights(double difference);

    void onDataInit();

    ActivationFunctionType getOutputActivationFunction();

    void initDifferenceHistory(int teachCycleCnt);
}
