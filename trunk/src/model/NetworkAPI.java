package model;

/**
 * Author: Sergey42
 * Date: 05.03.14 21:15
 */
public interface NetworkAPI {

    void fuzzyTeaching(int studyDataArrayLength);

    void fuzzyTeaching2(int era);

    double[] fuzzyForecast(int forecastSize);

    void initInputData(double[] data);
}
