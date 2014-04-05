package com.ssau.btc.gui;

import com.ssau.btc.model.ActivationFunctionType;
import com.ssau.btc.model.Network;
import com.ssau.btc.model.NetworkCreator;

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

}
