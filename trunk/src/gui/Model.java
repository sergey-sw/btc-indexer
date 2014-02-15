package gui;

import model.ActivationFunctionType;
import model.Network;
import model.NetworkCreator;
import model.Neuron;

/**
 * Author: Sergey42
 * Date: 14.02.14 23:20
 */
public class Model {

    private Controller controller;
    public Network network;

    public Model(Controller controller) {
        this.controller = controller;
    }

    public void createNetwork(int[] structure, ActivationFunctionType[] activationFunctionTypes, double[] parameters) {
        network = NetworkCreator.create(structure, activationFunctionTypes, parameters);
    }

    public void teachNetwork(int inputDataArrayLength) {
        network.initInputData(inputDataArrayLength);
        network.initNormalizedData();
        network.initDifferenceHistory();

        inputDataArrayLength = network.nInputs.length;


        network.studyLength = inputDataArrayLength;

        network.fuzzyTeaching(inputDataArrayLength);
        for (int era = 0; era < network.teachCycleCount; era++) {
            network.fuzzyTeaching2(era);
            controller.onTeachIterationPerformed(era);
        }
    }

    public void initInputData(int inputDataArrayLength) {
        network.initInputData(inputDataArrayLength);
        network.initNormalizedData();
    }

    public double[] forecast(int count)    {
        return network.fuzzyForecast(count);
    }
}
