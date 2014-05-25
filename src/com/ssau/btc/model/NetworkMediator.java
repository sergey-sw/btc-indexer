package com.ssau.btc.model;

import java.io.Serializable;

/**
 * Author: Sergey42
 * Date: 18.05.14 17:23
 */
public interface NetworkMediator extends Serializable {

    double[] calcNetOutput(double[] inputs);

    double correctWeights(double difference);

    void onDataInit();

    ActivationFunctionType getOutputActivationFunction();

    void initDifferenceHistory(int teachCycleCnt);
}
