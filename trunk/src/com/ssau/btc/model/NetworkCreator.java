package com.ssau.btc.model;

import com.ssau.btc.gui.Config;
import com.ssau.btc.utils.MathUtils;

import java.util.List;
import java.util.Random;

/**
 * Author: Sergey42
 * Date: 14.02.14 23:06
 */
public class NetworkCreator {

    public static Network create(List<LayerInfo> layerInfos) {

        int[] layers = new int[layerInfos.size()];
        ActivationFunctionType[] activationFunctionTypes = new ActivationFunctionType[layerInfos.size()];
        double[] params = new double[layerInfos.size()];

        for (int i = 0; i < layerInfos.size(); i++) {
            layers[i] = layerInfos.get(i).neuronCnt;
            activationFunctionTypes[i] = layerInfos.get(i).functionType;
            params[i] = layerInfos.get(i).coefficient;
        }

        Random random = new Random();

        int layersCount = layers.length - 1;
        int[] mArray = new int[layersCount];
        System.arraycopy(layers, 1, mArray, 0, layersCount);

        double[] mParams = new double[layersCount];
        System.arraycopy(params, 1, mParams, 0, layersCount);

        Network network = new Network();
        network.layerInfos = layerInfos;

        network.neuronInputs = new double[layersCount][];
        network.zeroArray = new double[layersCount][];
        network.inputsMLP = new double[layers[0]];
        network.neuronOutputs = new double[layersCount][];
        network.neuronDeltas = new double[layersCount][];
        network.neuronWeights = new double[layersCount][][];
        network.neuronWeightsM1 = new double[layersCount][][];
        network.neuronWeightsM2 = new double[layersCount][][];
        network.activationFunctionCoefficients = new double[mParams.length];
        network.activationFunctionTypes = new ActivationFunctionType[activationFunctionTypes.length];


        System.arraycopy(mParams, 0, network.activationFunctionCoefficients, 0, mParams.length);

        System.arraycopy(activationFunctionTypes, 0, network.activationFunctionTypes, 0, layers.length);

        for (int i = 0; i < layersCount; i++) {
            int neuronsCount = mArray[i];
            network.neuronInputs[i] = new double[neuronsCount];
            network.zeroArray[i] = new double[neuronsCount];
            network.neuronOutputs[i] = new double[neuronsCount];
            network.neuronDeltas[i] = new double[neuronsCount];

            network.neuronWeights[i] = new double[neuronsCount][];
            network.neuronWeightsM1[i] = new double[neuronsCount][];
            network.neuronWeightsM2[i] = new double[neuronsCount][];


            int prevLayerCount = (i == 0) ? network.inputsMLP.length : mArray[i - 1];

            for (int j = 0; j < neuronsCount; j++) {
                network.neuronWeights[i][j] = new double[prevLayerCount];
                network.neuronWeightsM1[i][j] = new double[prevLayerCount];
                network.neuronWeightsM2[i][j] = new double[prevLayerCount];
                for (int u = 0; u < prevLayerCount; u++) {
                    network.neuronWeights[i][j][u] = ((double) MathUtils.randInt(random, -99, 99)) / (100 * prevLayerCount);
                }
            }
        }
        // Fuzzy init
        int fCount = network.inputsMLP.length;
        network.fuzzyCenters = new double[fCount];

        network.fuzzyInputs = new double[fCount];
        network.fuzzyOutputs = new double[fCount];
        network.fuzzyWeights = new double[fCount][];
        for (int i = 0; i < fCount; i++) {
            network.fuzzyWeights[i] = new double[fCount];
            for (int j = 0; j < fCount; j++) {
                network.fuzzyWeights[i][j] = MathUtils.randInt(random, -99, 99) / (100 * fCount);
            }
            network.fuzzyCenters[i] = ((double) MathUtils.randInt(random, -99, 99)) / (100 * fCount);
        }


        return network;
    }

    public static Network buildDefault() {
        return create(Config.getDefaultStructure());
    }
}
