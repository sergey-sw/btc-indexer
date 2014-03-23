package com.ssau.btc.gui;

/**
 * Author: Sergey42
 * Date: 14.02.14 23:37
 */
public interface View {
    void displayTeachInfo(int iteration, double spentTime);

    void displayError(String message);

    void drawActivationFunction(double[] functionValues, Object datas);

    void drawDerivation(double[] derivateData, Object datas);

    void displayNetworkCreatedMessage();

    Object[] getTab2Data();

    void drawInputFunction(double[] fullArray);

    void drawForecast(double[] forecastValues, int inputLength, double[] forecastDataArray);

    void initProgressBar(int teachCycleCount);

    void drawAverageMistake(double[] averageMistakeData);

    void addStudyCbItems(int teachCycleCount);

    void displayFinishStudyMessage();

    Object[] GetAnalysisData();

    void drawMistakes(double[] values, double[] reals, double[] diffs);

    Object[] getDataForInputGraph();
}
