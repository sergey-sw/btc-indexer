package com.ssau.btc.model;

import java.io.Serializable;

/**
 * Author: Sergey42
 * Date: 05.03.14 21:15
 */
public interface NetworkAPI extends Serializable {

    void teach();

    double[] forecast(int forecastSize);

    void initInputData(double[] data);

    void initMultiDimensionData(double[] data1, double[] data2, double minData2, double maxData2);

    void setValue(String name, Object value);

    <T> T getValue(String name);

    double[] getAverageDiffPerEraHistory();

    double[][] getOutputHistory();

    double[][] getWeightChangeHistory();

    void setSpeedRate(double speedRate);
}
