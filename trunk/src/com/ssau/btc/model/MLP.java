package com.ssau.btc.model;

/**
 * Multi layer perceptron
 * <p/>
 * Author: Sergey42
 * Date: 18.05.14 16:54
 */
public class MLP {

    public double[] nData;
    public int dataLength;
    public int window;
    /**
     * First dimension - layer number
     * Second dimension - number of a neuron in a layer
     * Third dimension - number of a neuron in a PREVIOUS/NEXT layer
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
    public double speedRate;

    protected NetworkMediator networkMediator;

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
            default:
                throw new RuntimeException("Default case operator is a rudiment");
        }
    }

    protected double calcNetworkOutput(double[] inputValues) {
        int firstLayerNeuronCnt = neuronInputs[0].length;
        assert inputValues.length == firstLayerNeuronCnt;
        System.arraycopy(inputValues, 0, neuronOutputs[0], 0, firstLayerNeuronCnt);

        for (int i = 1; i < neuronInputs.length; i++) {
            for (int j = 0; j < neuronInputs[i].length; j++) {
                for (int k = 0; k < neuronInputs[i - 1].length; k++) {
                    neuronInputs[i][j] += neuronOutputs[i - 1][k] * neuronWeights[i][j][k];
                }
                neuronOutputs[i][j] = calcNeuronOutput(neuronInputs[i][j], i);
            }
        }
        return neuronOutputs[neuronOutputs.length - 1][0];
    }

    protected void teach(int eraNumber) {
        int iterationNumber = 0; // Номер итерации алгоритма внутри одной эпохи
        int outputLayerNumber = neuronOutputs.length - 1;

        // Копируем первые значения в массив истории выходов
        System.arraycopy(nData, 0, outputsHistory[eraNumber], 0, window);

        double[] inputs = new double[window];

        while (iterationNumber < dataLength - window - 1) {
            // Копируем участок выборки на вход сети
            System.arraycopy(nData, iterationNumber, inputs, 0, window);

            // Вычисление выходного сигнала при текущих весах и входных данных
            double output = networkMediator.calcNetOutput(inputs);

            // в классической сигмоиде нужно масштабировать значения
            if (activationFunctionTypes[outputLayerNumber] == ActivationFunctionType.C_SIGMOID) {
                output = (output - 0.5) * 2;
            }

            // Вычисление величины несоответствия
            double difference = output - nData[iterationNumber + window];

            differenceHistory[eraNumber][iterationNumber + window] = difference;
            outputsHistory[eraNumber][iterationNumber + window] = output;

            // Корректировка весов
            networkMediator.correctWeights(difference);

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

    protected void correctWeights(double difference) {
        int last = neuronInputs.length - 1;
        neuronDeltas[last][0] = difference *
                calcActivationFunctionDerivative(neuronOutputs[last][0], neuronInputs[last][0], last);

        // Проход по слоям - с предпоследнего до первого
        // Вычисление величины дельта
        for (int i = last - 1; i >= 1; i--) {
            // Проход по нейронам слоя
            for (int j = 0; j < neuronInputs[i].length; j++) {
                double rightSum = 0.0;
                for (int u = 0; u < neuronInputs[i + 1].length; u++) {
                    rightSum += neuronWeights[i + 1][u][j] * neuronDeltas[i + 1][u];
                }
                neuronDeltas[i][j] = rightSum *
                        calcActivationFunctionDerivative(neuronOutputs[i][j], neuronInputs[i][j], i);
            }
        }

        // Прямой проход по слоям, корректировка весов для I>1
        for (int i = 1; i < last + 1; i++) {
            for (int j = 0; j < neuronInputs[i].length; j++) {
                for (int u = 0; u < neuronInputs[i - 1].length; u++) {
                    double deltaW = neuronDeltas[i][j] * neuronOutputs[i - 1][u] * speedRate;
                    neuronWeights[i][j][u] -= deltaW;
                }
            }
        }
    }

    private double calcActivationFunctionDerivative(double output, double input, int layerId) {
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
            default:
                throw new RuntimeException("Default case operator is a rudiment");
        }
    }

    protected void resetCache() {
        for (int i = 0; i < zeroArray.length; i++) {
            System.arraycopy(zeroArray[i], 0, neuronInputs[i], 0, zeroArray[i].length);
        }
    }

    protected void initDifferenceHistory(int teachCycleCount) {
        differenceHistory = new double[teachCycleCount][];
        outputsHistory = new double[teachCycleCount][];

        for (int i = 0; i < differenceHistory.length; i++) {
            differenceHistory[i] = new double[dataLength];
            outputsHistory[i] = new double[dataLength];
        }
        averageDiffPerEraHistory = new double[teachCycleCount];
    }
}
