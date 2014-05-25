package com.ssau.btc.model;

import com.ssau.btc.utils.MultiDimensionHelper;

import java.io.Serializable;

/**
 * Multi layer perceptron
 * <p/>
 * Author: Sergey42
 * Date: 18.05.14 16:54
 */
public class MLP implements Serializable {

    private static final long serialVersionUID = 6033796215864382849L;
    public double[] nData;
    public double[] nData2;
    public int dataLength;
    public int inputWindow;
    public int outputWindow;
    /**
     * First dimension - layer number
     * Second dimension - number of a neuron in a layer
     * Third dimension - number of a neuron in a PREVIOUS layer
     */
    public double[][][] neuronWeights;
    public ActivationFunctionType[] activationFunctionTypes;
    public double[] activationFunctionCoefficients;
    public double[][] neuronInputs;
    public double[][] neuronOutputs;
    public double[][] zeroArray;
    public double[][] neuronDeltas;

    public double[][] differenceHistory;
    public double[][] outputsHistory;
    public double[] averageDiffPerEraHistory;
    public double[][] weightChangeHistory;
    public double speedRate;

    public boolean multiDimension;
    protected transient NetworkMediator networkMediator;

    protected void init(NetworkMediator mediator) {
        networkMediator = mediator;
    }

    protected double calcNeuronOutput(double input, int layerNo) {
        switch (activationFunctionTypes[layerNo]) {
            case C_SIGMOID:
                return 1.0 / (1 + Math.exp(-activationFunctionCoefficients[layerNo] * input));
            case R_SIGMOID:
                return input / (Math.abs(input) + activationFunctionCoefficients[layerNo]);
            case H_TANGENT: {
                double e2x = Math.exp(2 * activationFunctionCoefficients[layerNo] * input);
                return (e2x - 1) / (e2x + 1);
            }
            case SINUS:
                return Math.sin(input);
            case COS:
                return Math.cos(input);
            default:
                throw new RuntimeException("Default case operator is a rudiment");
        }
    }

    protected double calcActivationFunctionDerivative(double output, double input, int layerId) {
        ActivationFunctionType actFunctionType = activationFunctionTypes[layerId];
        switch (actFunctionType) {
            case R_SIGMOID:
                return (output + 1) * output / input;
            case C_SIGMOID:
                return activationFunctionCoefficients[layerId] * output * (1 - output);
            case H_TANGENT:
                return activationFunctionCoefficients[layerId] * (1 - output * output);
            case SINUS:
                return Math.cos(input);
            case COS:
                return -Math.sin(input);
            default:
                throw new RuntimeException("Default case operator is a rudiment");
        }
    }

    protected double[] calcNetworkOutput(double[] inputValues) {
        System.arraycopy(inputValues, 0, neuronOutputs[0], 0, inputWindow);

        for (int i = 1; i < neuronInputs.length; i++) {
            for (int j = 0; j < neuronInputs[i].length; j++) {
                for (int k = 0; k < neuronInputs[i - 1].length; k++) {
                    neuronInputs[i][j] += neuronOutputs[i - 1][k] * neuronWeights[i][j][k];
                }
                neuronOutputs[i][j] = calcNeuronOutput(neuronInputs[i][j], i);
            }
        }
        return neuronOutputs[neuronOutputs.length - 1];
    }

    protected void teach(int eraNumber) {
        int iterationNumber = 0; // Номер итерации алгоритма внутри одной эпохи
        int outputLayerNumber = neuronOutputs.length - 1;

        // Копируем первые значения в массив истории выходов
        System.arraycopy(nData, 0, outputsHistory[eraNumber], 0, inputWindow);

        double[] inputs = new double[inputWindow];
        double[] expected = new double[outputWindow];

        while (iterationNumber < dataLength - inputWindow - outputWindow) {
            // Копируем участок выборки на вход сети
            //System.arraycopy(nData, iterationNumber, inputs, 0, inputWindow);
            if (multiDimension) {
                inputs = MultiDimensionHelper.mixSingle(nData, iterationNumber, inputWindow - 1, nData2, iterationNumber, inputs);
            } else {
                System.arraycopy(nData, iterationNumber, inputs, 0, inputWindow);
            }

            // Вычисление выходного сигнала при текущих весах и входных данных
            double outputs[] = networkMediator.calcNetOutput(inputs);

            // в классической сигмоиде нужно масштабировать значения
            if (activationFunctionTypes[outputLayerNumber] == ActivationFunctionType.C_SIGMOID) {
                for (int i = 0; i < outputs.length; i++) {
                    outputs[i] = (outputs[i] - 0.5) * 2;
                }
            }

            if (multiDimension) {
                System.arraycopy(nData, iterationNumber + inputWindow - 1, expected, 0, outputWindow);
            } else {
                System.arraycopy(nData, iterationNumber + inputWindow, expected, 0, outputWindow);
            }
            // Вычисление величины несоответствия
            double difference = calcDifference(expected, outputs);

            differenceHistory[eraNumber][iterationNumber + inputWindow] = difference;
            outputsHistory[eraNumber][iterationNumber + inputWindow] = outputs[0];

            // Корректировка весов
            double weightChange = networkMediator.correctWeights(difference);
            weightChangeHistory[eraNumber][iterationNumber] = weightChange;

            // сброс внутренного состояния
            resetCache();
            iterationNumber++;
        }

        double sum = 0;
        for (int i = 0; i < differenceHistory[eraNumber].length; i++) {
            sum += (differenceHistory[eraNumber][i] * differenceHistory[eraNumber][i]);
        }
        averageDiffPerEraHistory[eraNumber] = Math.sqrt(sum / differenceHistory[eraNumber].length);
    }

    protected double calcDifference(double[] expected, double[] affected) {
        assert expected.length == affected.length;
        double diff = 0.0;
        for (int i = 0; i < expected.length; i++) {
            diff += (affected[i] - expected[i]);
        }
        return diff / expected.length;
    }

    protected double correctWeights(double difference) {
        int last = neuronInputs.length - 1;

        for (int i = 0; i < neuronOutputs[last].length; i++) {
            neuronDeltas[last][i] = difference *
                    calcActivationFunctionDerivative(neuronOutputs[last][i], neuronInputs[last][i], last);
        }

        // Проход по слоям - с предпоследнего до первого
        // Вычисление величины дельта
        for (int i = last - 1; i >= 1; i--) {
            // Проход по нейронам слоя
            for (int j = 0; j < neuronInputs[i].length; j++) {
                double rightSum = 0.0;
                for (int k = 0; k < neuronInputs[i + 1].length; k++) {
                    rightSum += neuronWeights[i + 1][k][j] * neuronDeltas[i + 1][k];
                }
                neuronDeltas[i][j] = rightSum *
                        calcActivationFunctionDerivative(neuronOutputs[i][j], neuronInputs[i][j], i);
            }
        }

        double deltaTotal = .0;
        // Прямой проход по слоям, корректировка весов для I>1
        for (int i = 1; i <= last; i++) {
            for (int j = 0; j < neuronInputs[i].length; j++) {
                for (int k = 0; k < neuronInputs[i - 1].length; k++) {
                    double deltaW = neuronDeltas[i][j] * neuronOutputs[i - 1][k] * speedRate;

                    neuronWeights[i][j][k] -= deltaW;
                    deltaTotal += Math.abs(deltaW);
                }
            }
        }
        return deltaTotal;
    }

    protected void resetCache() {
        for (int i = 0; i < zeroArray.length; i++) {
            System.arraycopy(zeroArray[i], 0, neuronInputs[i], 0, zeroArray[i].length);
        }
    }

    protected void initDifferenceHistory(int teachCycleCount) {
        differenceHistory = new double[teachCycleCount][];
        outputsHistory = new double[teachCycleCount][];
        weightChangeHistory = new double[teachCycleCount][];

        for (int i = 0; i < differenceHistory.length; i++) {
            differenceHistory[i] = new double[dataLength - outputWindow];
            outputsHistory[i] = new double[dataLength - outputWindow];
            weightChangeHistory[i] = new double[dataLength - inputWindow - outputWindow];
        }
        averageDiffPerEraHistory = new double[teachCycleCount];
    }
}
