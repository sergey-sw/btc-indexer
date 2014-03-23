package com.ssau.btc.model;

/**
 * Author: Sergey42
 * Date: 05.03.14 21:15
 */
public interface NetworkAPI {

    void teach();

    double[] fuzzyForecast(int forecastSize);

    void initInputData(double[] data);

    void setValue(String name, Object value);
}
