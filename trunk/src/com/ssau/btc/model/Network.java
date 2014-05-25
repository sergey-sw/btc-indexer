package com.ssau.btc.model;

import com.ssau.btc.utils.MathUtils;
import com.ssau.btc.utils.MultiDimensionHelper;

import java.util.List;

/**
 * Author: Sergey42
 * Date: 14.02.14 21:45
 */
public class Network implements NetworkAPI {

    private static final long serialVersionUID = 5201280714110288780L;

    public double[] data;
    public double[] nData;
    public int dataLength;
    public int window;

    public double[] data2;
    public double[] nData2;

    public int teachCycleCount;

    public double maxValue;
    public double minValue;

    public double maxValue2;
    public double minValue2;

    public NetState netState = NetState.NEW;

    public List<LayerInfo> layerInfos;

    protected MLP mlp;
    protected RBFLayer rbfLayer;

    protected boolean outputIsSigmoid;
    protected boolean multiDimension;

    private NetworkMediator networkMediator = new NetworkMediator() {

        @Override
        public double[] calcNetOutput(double[] inputs) {
            //double[] outputs = rbfLayer.calcRBFOutput(inputs);
            return mlp.calcNetworkOutput(inputs);
        }

        @Override
        public double correctWeights(double difference) {
            return mlp.correctWeights(difference);
        }

        @Override
        public void onDataInit() {
            mlp.nData = nData;
            mlp.dataLength = nData.length;
            rbfLayer.nData = nData;
            rbfLayer.dataLength = nData.length;

            if (multiDimension) {
                mlp.multiDimension = true;
                mlp.nData2 = nData2;
            }
        }

        @Override
        public ActivationFunctionType getOutputActivationFunction() {
            return mlp.activationFunctionTypes[mlp.activationFunctionTypes.length - 1];
        }

        @Override
        public void initDifferenceHistory(int teachCycleCnt) {
            mlp.initDifferenceHistory(teachCycleCnt);
        }
    };

    public void init() {
        mlp.init(networkMediator);
        rbfLayer.init(networkMediator);
    }

    /* Maps value from interval [A;B] to interval [-1;1] */
    private double normalize(double value) {
        return 2 * (value - 0.5 * (maxValue + minValue)) / (maxValue - minValue);
    }

    private double denormalize(double value, ActivationFunctionType type) {
        if (type == ActivationFunctionType.C_SIGMOID) {
            return (value + Math.abs(minValue)) / (maxValue - minValue);
        }
        return value * (maxValue - minValue) / 2 + 0.5 * (maxValue + minValue);
    }

    @Override
    public void teach() {
        if (netState != NetState.DATA_INITED) {
            throw new IllegalStateException("Data must be initialized before teaching. Current state : " + netState);
        }
        networkMediator.initDifferenceHistory(teachCycleCount);
        //rbfLayer.teach();
        for (int i = 0; i < teachCycleCount; i++) {
            mlp.teach(i);
        }
    }


    @Override
    public double[] forecast(int forecastSize) {
        double[] inputs = new double[window];
        // Массив предсказанных значений
        double[] forecast = new double[forecastSize + window];
        double[] approxNData2 = null;

        // Копируем последние значения исходных данных на вход сети
        if (multiDimension) {
            inputs = MultiDimensionHelper.mixSingle(nData, nData.length - window + 1, window - 1,
                    nData2, nData2.length - window, inputs);
        } else {
            System.arraycopy(nData, nData.length - window, inputs, 0, window);
        }

        // Первые {window} точек массива прогноза равны исходным значениям
        if (multiDimension) {
            System.arraycopy(nData, nData.length - window + 1, forecast, 0, window - 1);
        } else {
            System.arraycopy(inputs, 0, forecast, 0, window);
        }

        if (multiDimension) {
            approxNData2 = new double[window + forecastSize];
            approxNData2 = MultiDimensionHelper.approxSecondDimension(approxNData2, nData2, window);
        }

        // Начиная с позиции {window}
        for (int j = window; j < forecastSize + window; j++) {
            // Задание входных значений нейронам входного слоя
            if (multiDimension) {
                inputs = MultiDimensionHelper.mixSingle(forecast, j - window, window - 1, approxNData2, j - window, inputs);
            } else {
                System.arraycopy(forecast, j - window, inputs, 0, window);
            }

            // Вычисление выходного значения
            double[] outputs = networkMediator.calcNetOutput(inputs);

            if (outputIsSigmoid) {
                for (int i = 0; i < outputs.length; i++) {
                    outputs[i] = (outputs[i] - 0.5) * 2;
                }
            }

            forecast[j] = outputs[0];

            mlp.resetCache();
        }

        ActivationFunctionType type = networkMediator.getOutputActivationFunction();
        for (int i = window; i < forecast.length; i++) {
            forecast[i] = denormalize(forecast[i], type);
        }

        double[] onlyForecast = new double[forecastSize];
        System.arraycopy(forecast, window, onlyForecast, 0, forecastSize);

        return onlyForecast;
    }

    @Override
    public void initInputData(double[] data) {
        dataLength = data.length;
        this.data = data;

        maxValue = MathUtils.findMax(this.data);
        minValue = MathUtils.findMin(this.data);

        /* init normalized data */
        nData = new double[this.data.length];
        for (int i = 0; i < nData.length; i++) {
            nData[i] = normalize(this.data[i]);
        }
        netState = NetState.DATA_INITED;
        networkMediator.onDataInit();
    }

    @Override
    public void initMultiDimensionData(double[] data1, double[] data2, double minData2, double maxData2) {
        assert data1.length == data2.length;
        multiDimension = true;

        dataLength = data1.length;
        this.data = data1;
        this.data2 = data2;

        maxValue = MathUtils.findMax(this.data);
        minValue = MathUtils.findMin(this.data);

        minValue2 = minData2;
        maxValue2 = maxData2;

        /* init normalized data */
        nData = new double[this.data.length];
        nData2 = new double[this.data2.length];
        for (int i = 0; i < nData.length; i++) {
            nData[i] = normalize(this.data[i]);
            nData2[i] = MathUtils.normalize(this.data2[i], minData2, maxData2);
        }

        netState = NetState.DATA_INITED;
        networkMediator.onDataInit();
    }

    @Override
    public void setValue(String name, Object value) {
        try {
            getClass().getField(name).set(this, value);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getValue(String name) {
        try {
            return (T) getClass().getField(name).get(this);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public double[] getAverageDiffPerEraHistory() {
        return mlp.averageDiffPerEraHistory;
    }

    @Override
    public double[][] getOutputHistory() {
        return mlp.outputsHistory;
    }

    @Override
    public double[][] getWeightChangeHistory() {
        return mlp.weightChangeHistory;
    }

    @Override
    public void setSpeedRate(double speedRate) {
        mlp.speedRate = speedRate;
    }
}
