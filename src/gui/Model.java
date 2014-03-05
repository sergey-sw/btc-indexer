package gui;

import model.ActivationFunctionType;
import model.Network;
import model.NetworkCreator;

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

    public void teachNetwork() {
        network.initDifferenceHistory();

        //network.fuzzyTeaching();
        for (int era = 0; era < network.teachCycleCount; era++) {
            network.fuzzyTeaching2(era);
            controller.onTeachIterationPerformed(era);
        }
    }

    public double[] forecast(int count) {
        return network.fuzzyForecast(count);
    }
}
