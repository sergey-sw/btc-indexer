package com.ssau.btc.model;

import com.ssau.btc.utils.MathUtils;

import java.util.List;

/**
 * Author: Sergey42
 * Date: 14.02.14 21:45
 */
public class Network implements NetworkAPI {

    private static final long serialVersionUID = 5201280714110288780L;

    /* Array of input values */
    public double[] data;
    public double[] nData;
    public int dataLength;
    public int window;

    public double[][] differenceHistory;
    public double[][] outputsHistory;
    public double[] averageDiffPerEraHistory;

    public int teachCycleCount;
    public double speedRate;

    public ActivationFunctionType[] activationFunctionTypes;
    /* for 8-16-1 net array is 2:16:8*/
    public double[][][] neuronWeights;

    //public double[] inputsMLP;
    //public double[][] neuronInputs;
    /*public double[][] neuronOutputs;
    public double[][] zeroArray;

    public double[] activationFunctionCoefficients;

    public double[][] neuronDeltas;

    public double[] fuzzyInputs;
    public double[] fuzzyOutputs;
    public double[][] fuzzyWeights;
    public double[] fuzzyCenters;
    *//* Dimension M - input data count, N - input layer count *//*
    public double[][] fuzzyBelongs;*/

    public double maxValue;
    public double minValue;

    //private Random random = new Random();

    public NetState netState = NetState.NEW;

    public List<LayerInfo> layerInfos;

    protected MLP mlp;
    protected RBFLayer rbfLayer;

    protected boolean outputIsSigmoid;

    private NetworkMediator networkMediator = new NetworkMediator() {

        @Override
        public double calcNetOutput(double[] inputs) {
            double[] outputs = rbfLayer.calcRBFOutput(inputs);
            return mlp.calcNetworkOutput(outputs);
        }

        @Override
        public void correctWeights(double difference) {
            fuzzyCorrectWeights(difference);
        }

        @Override
        public void onDataInit() {
            mlp.nData = nData;
            mlp.dataLength = nData.length;
            rbfLayer.nData = nData;
            rbfLayer.dataLength = nData.length;
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

    /*private double calcNeuronOutput(double input, int layerNo) {
        if (layerNo == 0) {
            layerNo = 1;
        }

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
    }*/

    private double fuzzyCalculateNetOutput(double[] inputs) {
        return networkMediator.calcNetOutput(inputs);
        /*int inputCount = inputsMLP.length;
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
            neuronOutputs[0][u] = calcNeuronOutput(neuronInputs[0][u], 1);  //todo i replace layerNo 0 with 1

            int zLength = neuronInputs[1].length;
            for (int z = 0; z < zLength; z++) {
                neuronInputs[1][z] += neuronOutputs[0][u] * neuronWeights[1][z][u];
            }
        }

        // Проход по всем слоям нейронной сети c 1 todo i replace 1 with 0
        for (int i = 0; i < neuronInputs.length; i++) {
            int jLength = neuronInputs[i].length;
            // Проход по всем нейронам текущего слоя
            for (int j = 0; j < jLength; j++) {
                neuronOutputs[i][j] = calcNeuronOutput(neuronInputs[i][j], i + 1); //todo i add +1

                // Проход по нейронам, явл-ся приемниками сигнала от neuron
                if (i >= neuronInputs.length - 1) continue;
                int i1Length = neuronInputs[i + 1].length;
                for (int u = 0; u < i1Length; u++) {
                    neuronInputs[i + 1][u] += neuronOutputs[i][j] * neuronWeights[i + 1][u][j];
                }
            }
        }*/
    }

   /* private void fuzzyInitBelongs() {
        fuzzyBelongs = new double[dataLength][];
        for (int i = 0; i < fuzzyBelongs.length; i++) {
            fuzzyBelongs[i] = new double[inputsMLP.length];
        }

        for (int f = 0; f < fuzzyBelongs.length; f++) {
            boolean successInit = false;
            while (!successInit) {
                double tempSum = 0.0;
                for (int i = 0; i < inputsMLP.length - 1; i++) {
                    tempSum += fuzzyBelongs[f][i] = random.nextDouble() * 2 / inputsMLP.length;
                }

                if (tempSum >= 1) continue;

                fuzzyBelongs[f][inputsMLP.length - 1] = (1 - tempSum);
                successInit = true;
            }
        }
    }*/

    /*private static double calcBelongToCenter(double[] inputs, double center) {
        double sum = 0.0;
        for (double input : inputs) {
            sum += (center - input) * (center - input);
        }
        return sum;
    }*/

    // C-means
    /* Seems that is teaches RBF layer only */
    /*private void fuzzyTeaching() {
        // init belong coefficients
        fuzzyInitBelongs();

        boolean errorIsOk = false;
        int iterationNumber = 0;
        double errorBarrier = Config.CMEANS_ERROR_BARRIER;
        int iterationsMaxNumber = Config.CMEANS_MAX_ITERATIONS;

        fuzzyTeachErrorHistory = new double[iterationsMaxNumber];

        while (!errorIsOk && iterationNumber < iterationsMaxNumber) {
            // Расчет центров
            // Цикл по нейронам fuzzy слоя
            for (int j = 0; j < inputsMLP.length; j++) {
                double tempUpSum = 0.0;
                double tempDownSum = 0.0;

                // Цикл по входной выборке
                double belongSquare;
                for (int i = 0; i < dataLength; i++) {
                    belongSquare = fuzzyBelongs[i][j] * fuzzyBelongs[i][j];
                    tempUpSum += belongSquare * nData[i];
                    tempDownSum += belongSquare;
                }

                fuzzyCenters[j] = tempUpSum / tempDownSum;
            }

            double error = 0.0;
            double temp1;
            double temp2;

            double distance;
            for (int f = 0; f < inputsMLP.length; f++) {
                for (int i = 0; i < dataLength; i++) {
                    temp1 = fuzzyBelongs[i][f];
                    distance = fuzzyCenters[f] - nData[i];
                    temp2 = distance * distance;
                    error += temp1 * temp1 * temp2 * temp2;
                }
            }

            fuzzyTeachErrorHistory[iterationNumber] = error;

            // Если ошибка мала, заканчиваем
            if (error < errorBarrier) {
                errorIsOk = true;
                continue;
            }

            // Пересчет коэф-ов принадлежности
            for (int f = 0; f < inputsMLP.length; f++) {
                for (int i = 0; i < dataLength; i++) {
                    // еще одна сумма
                    double sum = 0.0;
                    distance = fuzzyCenters[f] - nData[i];
                    double tmp1 = distance * distance * distance * distance;
                    double tmp2;

                    for (int k = 0; k < inputsMLP.length; k++) {
                        distance = fuzzyCenters[k] - nData[i];
                        tmp2 = distance * distance * distance * distance;
                        sum += tmp1 / tmp2;
                    }

                    fuzzyBelongs[i][f] = 1 / sum;
                }
            }
            iterationNumber++;
        }
    }*/

    @Override
    public void teach() {
        if (netState != NetState.DATA_INITED) {
            throw new IllegalStateException("Data must be initialized before teaching. Current state : " + netState);
        }
        networkMediator.initDifferenceHistory(teachCycleCount);
        rbfLayer.teach();//fuzzyTeaching();
        for (int i = 0; i < teachCycleCount; i++) {
            mlp.teach(i);//fuzzyTeachingMain(i);
        }
    }

    /*
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
    */
    private void fuzzyCorrectWeights(double difference) {
        mlp.correctWeights(difference);
        /*int last = neuronDeltas.length - 1;
        neuronDeltas[last][0] = difference *
                calcActivationFunctionDerivative(neuronOutputs[last][0], neuronInputs[last][0], last + 1); //todo i made lst + 1

        // Проход по слоям - с предпоследнего до первого
        // Вычисление величины дельта
        for (int i = last - 1; i >= 0; i--) {
            int iLength = neuronInputs[i].length;
            // Проход по нейронам слоя
            int uLength = neuronInputs[i + 1].length;
            for (int j = 0; j < iLength; j++) {
                double rightSum = 0.0;
                for (int u = 0; u < uLength; u++) {
                    rightSum += neuronWeights[i + 1][u][j] * neuronDeltas[i + 1][u];
                }
                neuronDeltas[i][j] = rightSum *
                        //todo i put +1 to last param
                        calcActivationFunctionDerivative(neuronOutputs[i][j], neuronInputs[i][j], i + 1);
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
        }*/
    }

    /*
    private void fuzzyTeachingMain(int eraNumber) {
        int iterationNumber = 0; // Номер итерации алгоритма внутри одной эпохи
        int inputLayerNeuronCount = inputsMLP.length; // кол-во нейронов входного слоя
        int outputLayerNumber = neuronOutputs.length - 1;

        // Копируем первые значения в массив истории выходов
        System.arraycopy(nData, 0, outputsHistory[eraNumber], 0, inputLayerNeuronCount);

        while (iterationNumber < dataLength - inputLayerNeuronCount - 1) {
            // Копируем участок выборки на вход сети
            double[] inputs = new double[inputLayerNeuronCount];
            System.arraycopy(nData, iterationNumber, inputs*//*inputsMLP*//*, 0, inputLayerNeuronCount);

            // Вычисление выходного сигнала при текущих весах и входных данных
            fuzzyCalculateNetOutput(inputs);

            double output = neuronOutputs[outputLayerNumber][0];

            // в классической сигмоиде нужно масштабировать значения
            if (activationFunctionTypes[outputLayerNumber] == ActivationFunctionType.C_SIGMOID) {
                output = (output - 0.5) * 2;
            }

            // Вычисление величины несоответствия
            double difference = output - nData[iterationNumber + inputLayerNeuronCount];

            differenceHistory[eraNumber][iterationNumber + inputLayerNeuronCount] = difference;
            outputsHistory[eraNumber][iterationNumber + inputLayerNeuronCount] = output;

            // Корректировка весов
            fuzzyCorrectWeights(difference);

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
    */

    private void resetCache() {
        /*for (int i = 0; i < zeroArray.length; i++) {
            System.arraycopy(zeroArray[i], 0, neuronInputs[i], 0, zeroArray[i].length);
        }*/
        mlp.resetCache();
    }

    @Override
    public double[] fuzzyForecast(int forecastSize) {
        double[] copyInput = new double[window];
        System.arraycopy(nData, nData.length - window, copyInput, 0, window);

        // Массив предсказанных значений
        double[] forecast = new double[forecastSize + window];

        // Первые {inputCount} точек массива прогноза равны исходным значениям
        System.arraycopy(copyInput, 0, forecast, 0, window);

        //int outputId = neuronOutputs.length - 1;

        double[] inputs = new double[window];
        // Начиная с позиции {Число нейронов входного слоя}
        for (int j = window; j < forecastSize + window; j++) {
            // Задание входных значений нейронам входного слоя
            System.arraycopy(forecast, j - window, inputs/*inputsMLP*/, 0, window);

            // Вычисление выходного значения
            double output = networkMediator.calcNetOutput(inputs);

            if (outputIsSigmoid/*activationFunctionTypes[outputId] == ActivationFunctionType.C_SIGMOID*/) {
                output = (output - 0.5) * 2;
            }

            forecast[j] = output;

            // Корректировка весов
            if (j < nData.length) {
                double diff = forecast[j] - nData[j];
                fuzzyCorrectWeights(diff);
            }

            resetCache();
        }

        ActivationFunctionType type = networkMediator.getOutputActivationFunction();/*activationFunctionTypes[activationFunctionTypes.length - 1];*/
        for (int i = 0; i < forecast.length; i++) {
            forecast[i] = denormalize(forecast[i], type);
        }

        double[] onlyForecast = new double[forecastSize];
        System.arraycopy(forecast, window, onlyForecast, 0, forecastSize);

        return onlyForecast;
    }

    /*private void initDifferenceHistory() {
        differenceHistory = new double[teachCycleCount][];
        outputsHistory = new double[teachCycleCount][];

        for (int i = 0; i < differenceHistory.length; i++) {
            differenceHistory[i] = new double[dataLength];
            outputsHistory[i] = new double[dataLength];
        }
        averageDiffPerEraHistory = new double[teachCycleCount];
    }*/

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
}
