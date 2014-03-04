package model;

import java.util.Random;

/**
 * Author: Sergey42
 * Date: 14.02.14 21:45
 */
public class Network {

    public double[] inputs;
    public int studyLength;

    public int fullLength;

    public double[] nInputs;

    public double[] studyInputs;
    public double[] forecastInputs;

    public int teachCycleCount;

    public double[][] differenceHistory;
    public double[][] outputsHistory;

    public double[] averageDiffPerEraHistory;

    public double speedRate;
    public boolean useMoments;

    public ActivationFunctionType[] activationFunctionTypes;

    public int layerCount;

    public double[][][] neuronWeights;

    public double[][][] neuronWeightsM1;
    public double[][][] neuronWeightsM2;

    public double[] inputsMLP;
    public double[][] neuronInputs;

    public double[][] zeroArray;

    public double[][] neuronOutputs;
    public double[] activationFunctionCoefficients;

    public double[][] neuronDeltas;

    public double[] fuzzyInputs;
    public double[] fuzzyOutputs;
    public double[][] fuzzyWeights;
    public double[] fuzzyCenters;
    public double[][] fuzzyBelongs;

    public double maxValue;
    public double minValue;

    private Random random = new Random();

    private double normalize(double value) {
        return 2 * (value - 0.5 * (maxValue + minValue)) / (maxValue - minValue);
    }

    private double denormalize(double value, ActivationFunctionType type) {
        if (type == ActivationFunctionType.C_SIGMOID) {
            return (value + Math.abs(minValue)) / (maxValue - minValue);
        }
        return value * (maxValue - minValue) / 2 + 0.5 * (maxValue + minValue);
    }

    private double calcNeuronOutput(double input, int layerNo) {
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

    private void calcNetOutput() {
        // Если рассматриваем входной слой -> копируем вход на выход
        System.arraycopy(neuronInputs[0], 0, neuronOutputs[0], 0, neuronInputs[0].length);

        int inputLayerLength = neuronInputs[0].length;
        int secondLayerLength = neuronInputs[1].length;

        // Рассчет входа скрытого слоя
        for (int u = 0; u < secondLayerLength; u++) {
            for (int j = 0; j < inputLayerLength; j++) {
                neuronInputs[1][u] = neuronOutputs[0][j] * neuronWeights[1][u][j];
            }
        }

        // Проход по всем слоям нейронной сети c 1
        for (int i = 1; i < neuronInputs.length; i++) {
            // Проход по всем нейронам текущего слоя
            for (int j = 0; j < neuronInputs[i].length; j++) {
                neuronOutputs[i][j] = calcNeuronOutput(neuronInputs[i][j], i);

                // Проход по нейронам, явл-ся приемниками сигнала от neuron
                if (i >= neuronInputs.length - 1) continue;

                for (int u = 0; i < neuronInputs[i + 1].length; u++) {
                    neuronInputs[i + 1][u] += neuronOutputs[i][j] * neuronWeights[i + 1][u][j];
                }
            }
        }
    }

    private void fuzzyCalculateNetOutput() {
        int inputCount = inputsMLP.length;
        int fuzzyCount = fuzzyInputs.length;

        // кэшируем расстояния
        double[] belongsCache = new double[fuzzyCount];
        for (int z = 0; z < fuzzyCount; z++) {
            belongsCache[z] = calcBelongToCenter(inputsMLP, fuzzyCenters[z]);
        }

        // Выход нечеткого слоя
        for (int i = 0; i < inputCount; i++) {
            double top = belongsCache[i];
            top *= top;
            double sum = 0.0;
            for (int k = 0; k < fuzzyCount; k++) {
                double tDistance = belongsCache[k];
                sum += top / (tDistance * tDistance);
            }
            fuzzyOutputs[i] = 1 / sum;
        }

        int hiddenCount = neuronInputs[0].length;

        // Рассчет первого скрытого слоя
        for (int u = 0; u < hiddenCount; u++) {
            for (int j = 0; j < fuzzyCount; j++) {
                neuronInputs[0][u] += fuzzyOutputs[j] * neuronWeights[0][u][j];
            }
            neuronOutputs[0][u] = calcNeuronOutput(neuronInputs[0][u], 0);

            int zLength = neuronInputs[1].length;
            for (int z = 0; z < zLength; z++) {
                neuronInputs[1][z] += neuronOutputs[0][u] * neuronWeights[1][z][u];
            }
        }

        // Проход по всем слоям нейронной сети c 1
        for (int i = 1; i < neuronInputs.length; i++) {
            int jLength = neuronInputs[i].length;
            // Проход по всем нейронам текущего слоя
            for (int j = 0; j < jLength; j++) {
                neuronOutputs[i][j] = calcNeuronOutput(neuronInputs[i][j], i);

                // Проход по нейронам, явл-ся приемниками сигнала от neuron
                if (i >= neuronInputs.length - 1) continue;
                int i1Length = neuronInputs[i + 1].length;
                for (int u = 0; u < i1Length; u++) {
                    neuronInputs[i + 1][u] += neuronOutputs[i][j] * neuronWeights[i + 1][u][j];
                }
            }
        }
    }

    private void fuzzyInitBelongs(int studyDataArrayLength) {
        fuzzyBelongs = new double[studyDataArrayLength][];

        for (int i = 0; i < fuzzyBelongs.length; i++) {
            fuzzyBelongs[i] = new double[inputsMLP.length];
        }

        for (int f = 0; f < fuzzyBelongs.length; f++) {
            boolean successInit = false;

            int count = inputsMLP.length;

            while (!successInit) {
                double tempSum = 0.0;
                for (int i = 0; i < count - 1; i++) {
                    tempSum += fuzzyBelongs[f][i] = random.nextDouble() * 2 / count;
                }

                if (tempSum >= 1) continue;

                fuzzyBelongs[f][count - 1] = (1 - tempSum);
                successInit = true;
            }
        }
    }

    private static double calcBelongToCenter(double[] inputs, double center) {
        double sum = 0.0;
        double temp;
        for (double input : inputs) {
            temp = (center - input);
            sum += temp * temp;
        }

        return sum;
    }

    private static double radius(double c, double x) {
        return (x - c) * (x - c);
    }

    // C-means
    public void fuzzyTeaching(int studyDataArrayLength) {
        // init belong coefficients
        fuzzyInitBelongs(studyDataArrayLength);

        int fuzzyCount = inputsMLP.length;

        boolean errorIsOk = false;
        int iterations = 0;
        final double errorBarrier = 0.0001;
        final int iterationsMaxNumber = 50;

        double[] fuzzyTeachErrorHistory = new double[iterationsMaxNumber];

        while (!errorIsOk && iterations < iterationsMaxNumber) {
            // Расчет центров
            // Цикл по нейронам fuzzy слоя
            for (int j = 0; j < fuzzyCount; j++) {
                double tempUpSum = 0.0;
                double tempDownSum = 0.0;

                // Цикл по входной выборке
                for (int i = 0; i < studyDataArrayLength; i++) {
                    tempUpSum += fuzzyBelongs[i][j] * fuzzyBelongs[i][j] * nInputs[i];
                    tempDownSum += fuzzyBelongs[i][j] * fuzzyBelongs[i][j];
                }

                fuzzyCenters[j] = tempUpSum / tempDownSum;
            }

            double error = 0.0;
            double temp1 = 0.0;
            double temp2 = 0.0;

            double distance = 0.0;
            for (int f = 0; f < fuzzyCount; f++) {
                for (int i = 0; i < studyDataArrayLength; i++) {
                    temp1 = fuzzyBelongs[i][f];
                    distance = fuzzyCenters[f] - nInputs[i];
                    temp2 = distance * distance;

                    error += temp1 * temp1 * temp2 * temp2;
                }
            }

            fuzzyTeachErrorHistory[iterations] = error;

            // Если ошибка мала, заканчиваем
            if (error < errorBarrier) {
                errorIsOk = true;
                continue;
            }

            // Пересчет коэф-ов принадлежности
            for (int f = 0; f < fuzzyCount; f++) {
                for (int i = 0; i < studyDataArrayLength; i++) {
                    // еще одна сумма
                    double sum = 0.0;
                    distance = fuzzyCenters[f] - nInputs[i];
                    double tmp1 = distance * distance;
                    tmp1 *= tmp1;
                    double tmp2 = 0.0;

                    for (int k = 0; k < fuzzyCount; k++) {
                        distance = fuzzyCenters[k] - nInputs[i];
                        tmp2 = distance * distance;
                        tmp2 *= tmp2;

                        sum += tmp1 / tmp2;
                    }

                    fuzzyBelongs[i][f] = 1 / sum;
                }
            }
            iterations++;
        }

        int l = fuzzyTeachErrorHistory.length;
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

    private void fuzzyCorrectWeights(double difference) {
        int last = neuronDeltas.length - 1;
        neuronDeltas[last][0] = difference * calcActivationFunctionDerivative(neuronOutputs[last][0], neuronInputs[last][0], last);

        // Проход по слоям - с предпоследнего до первого
        // Вычисление величины дельта
        for (int i = last - 1; i >= 0; i--) {
            int iLength = neuronInputs[i].length;
            // Проход по нейронам слоя
            for (int j = 0; j < iLength; j++) {
                double rightSum = 0.0;
                int uLength = neuronInputs[i + 1].length;
                for (int u = 0; u < uLength; u++) {
                    rightSum += neuronWeights[i + 1][u][j] * neuronDeltas[i + 1][u];
                }
                neuronDeltas[i][j] = rightSum *
                        calcActivationFunctionDerivative(neuronOutputs[i][j], neuronInputs[i][j], i);
            }
        }
        //Корректировка для I=0
        int inputLength = inputsMLP.length;
        int kLength = neuronInputs[0].length;

        for (int k = 0; k < kLength; k++) {
            for (int t = 0; t < inputLength; t++) {
                double deltaW = neuronDeltas[0][k] * fuzzyOutputs[t] * speedRate;
                neuronWeights[0][k][t] -= deltaW;
            }
        }

        // Прямой проход по слоям, корректировка весов для I>1
        for (int i = 1; i < last + 1; i++) {
            int jLength = neuronInputs[i].length;
            for (int j = 0; j < jLength; j++) {
                int uLength = neuronInputs[i - 1].length;
                for (int u = 0; u < uLength; u++) {
                    double deltaW = neuronDeltas[i][j] * neuronOutputs[i - 1][u] * speedRate;
                    neuronWeights[i][j][u] -= deltaW;
                }
            }
        }
    }

    public void fuzzyTeaching2(int era) {
        int iteration = 0; // Номер итерации алгоритма
        int inputLayerNeuronCount = inputsMLP.length; // кол-во нейронов входного слоя
        int outputId = neuronOutputs.length - 1;

        while (iteration < studyLength - inputLayerNeuronCount - 1) {
            // Задание входных данных
            System.arraycopy(nInputs, iteration, inputsMLP, 0, inputLayerNeuronCount);

            // Вычисление выходного сигнала при текущих весах и входных данных
            fuzzyCalculateNetOutput();

            double output = neuronOutputs[outputId][0];

            // в классической сигмоиде нужно масштабировать значения
            if (activationFunctionTypes[outputId] == ActivationFunctionType.C_SIGMOID) {
                output = (output - 0.5) * 2;
            }

            // Вычисление величины несоответствия
            double difference = output - nInputs[iteration + inputLayerNeuronCount];

            differenceHistory[era][iteration] = difference;
            outputsHistory[era][iteration] = output;

            // Корректировка весов
            fuzzyCorrectWeights(difference);

            // сброс внутренних сигналов
            resetCache();

            iteration++;
        }

        double sum = 0;
        for (int i = 0; i < differenceHistory[era].length; i++) {
            sum += differenceHistory[era][i];
        }
        averageDiffPerEraHistory[era] = sum / differenceHistory[era].length - 1;
        averageDiffPerEraHistory[era] = Math.sqrt(averageDiffPerEraHistory[era]);
    }

    private void resetCache() {
        for (int i = 0; i < zeroArray.length; i++) {
            System.arraycopy(zeroArray[i], 0, neuronInputs[i], 0, zeroArray[i].length);
        }
    }

    public double[] fuzzyForecast(int forecastSize) {
        initForecastData();

        int inputLayerNeuronCount = inputsMLP.length;

        // Массив предсказанных значений
        double[] forecast = new double[forecastSize + inputLayerNeuronCount];

        // Первые {inputCount} точек массива прогноза равны исходным значениям
        System.arraycopy(nInputs, 0, forecast, 0, inputLayerNeuronCount);

        int outputId = neuronOutputs.length - 1;

        // Начиная с позиции {Число нейронов входного слоя}
        for (int j = inputLayerNeuronCount; j < forecastSize + inputLayerNeuronCount; j++) {
            // Задание входных значений нейронам входного слоя
            if (j < nInputs.length) {
                System.arraycopy(nInputs, j - inputLayerNeuronCount, inputsMLP, 0, inputLayerNeuronCount);
            } else {
                System.arraycopy(forecast, j - inputLayerNeuronCount, inputsMLP, 0, inputLayerNeuronCount);
            }

            // Вычисление выходного значения
            fuzzyCalculateNetOutput();
            double output = neuronOutputs[outputId][0];

            if (activationFunctionTypes[outputId] == ActivationFunctionType.C_SIGMOID) {
                output = (output - 0.5) * 2;
            }

            forecast[j] = output;

            // Корректировка весов
            if (j < nInputs.length) {
                double diff = forecast[j] - nInputs[j];
                fuzzyCorrectWeights(diff);
            }

            resetCache();
        }

        ActivationFunctionType type = activationFunctionTypes[activationFunctionTypes.length - 1];
        for (int i = 0; i < forecast.length; i++) {
            forecast[i] = denormalize(forecast[i], type);
        }

        return forecast;
    }

    private void initForecastData() {
        studyInputs = new double[inputs.length];
        System.arraycopy(inputs, 0, studyInputs, 0, inputs.length);

        double[] maxValues;
        double[] minValues;

        int period = 100; //TODO

        maxValues = new double[period];
        minValues = new double[period];

        forecastInputs = new double[maxValues.length];

        for (int i = 0; i < forecastInputs.length; i++) {
            forecastInputs[i] = (maxValues[i] + minValues[i]) / 2;
        }
        maxValue = MathUtils.findMax(forecastInputs);
        minValue = MathUtils.findMin(forecastInputs);

        fullLength += forecastInputs.length;

        initNormalizedData();
    }

    public void initNormalizedData() {
        nInputs = new double[inputs.length];
        for (int i = 0; i < nInputs.length; i++) {
            nInputs[i] = normalize(inputs[i]);
        }
    }

    public void initDifferenceHistory() {
        differenceHistory = new double[teachCycleCount][];
        outputsHistory = new double[teachCycleCount][];
        int inputDataArrayLength = inputs.length;
        int neuronInputsCount = inputsMLP.length;
        for (int i = 0; i < differenceHistory.length; i++) {
            differenceHistory[i] = new double[inputDataArrayLength - neuronInputsCount];
            outputsHistory[i] = new double[inputDataArrayLength - neuronInputsCount];
        }
        averageDiffPerEraHistory = new double[teachCycleCount];
    }

    public void initInputData(int inputDataArrayLength) {
        inputs = new double[inputDataArrayLength];
        fullLength = inputDataArrayLength;


        double[] minT = new double[inputDataArrayLength];
        double[] maxT = new double[inputDataArrayLength]; //TODO


        inputs = new double[maxT.length];
        fullLength = inputs.length;

        double _normalizeCoeff = 0.0;

        for (int i = 0; i < inputs.length; i++) {
            double temp = inputs[i] = (maxT[i] + minT[i]) / 2;
            _normalizeCoeff += temp * temp;
        }

        maxValue = MathUtils.findMax(inputs);
        minValue = MathUtils.findMin(inputs);
        _normalizeCoeff = Math.sqrt(_normalizeCoeff);
    }
}
