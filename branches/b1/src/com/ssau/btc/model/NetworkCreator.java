package com.ssau.btc.model;

import com.ssau.btc.sys.Config;
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

        Network network = new Network();
        network.layerInfos = layerInfos;
        MLP mlp = network.mlp = new MLP();
        RBFLayer rbfLayer = network.rbfLayer = new RBFLayer();
        network.init();

        network.window = mlp.inputWindow = rbfLayer.size = layers[0];
        mlp.outputWindow = layers[layers.length - 1];

        mlp.neuronInputs = new double[layerInfos.size()][];
        mlp.zeroArray = new double[layerInfos.size()][];
        //network.inputsMLP = new double[layers[0]];
        mlp.neuronOutputs = new double[layerInfos.size()][];
        mlp.neuronDeltas = new double[layerInfos.size()][];
        mlp.neuronWeights = new double[layerInfos.size()][][];
        mlp.activationFunctionCoefficients = params;
        mlp.activationFunctionTypes = activationFunctionTypes;

        for (int i = 0; i < layerInfos.size(); i++) {
            int neuronsCount = layers[i];
            mlp.neuronInputs[i] = new double[neuronsCount];
            mlp.zeroArray[i] = new double[neuronsCount];
            mlp.neuronOutputs[i] = new double[neuronsCount];
            mlp.neuronDeltas[i] = new double[neuronsCount];
            mlp.neuronWeights[i] = new double[neuronsCount][];

            int prevLayerCount = (i == 0) ? network.window : layers[i - 1];

            for (int j = 0; j < neuronsCount; j++) {
                mlp.neuronWeights[i][j] = new double[prevLayerCount];
                for (int u = 0; u < prevLayerCount; u++) {
                    mlp.neuronWeights[i][j][u] = ((double) MathUtils.randInt(random, -99, 99)) / (100 * prevLayerCount);
                }
            }
        }

        network.outputIsSigmoid = mlp.activationFunctionTypes[layers.length - 1] == ActivationFunctionType.C_SIGMOID;

        // Fuzzy init        
        rbfLayer.fuzzyCenters = new double[network.window];
        rbfLayer.fuzzyInputs = new double[network.window];
        rbfLayer.fuzzyOutputs = new double[network.window];
        for (int i = 0; i < network.window; i++) {
            rbfLayer.fuzzyCenters[i] = ((double) MathUtils.randInt(random, -99, 99)) / 100;
        }

        return network;
    }

    public static Network buildDefault() {
        return create(Config.getDefaultStructure());
    }
}
