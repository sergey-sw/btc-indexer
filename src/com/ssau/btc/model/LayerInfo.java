package com.ssau.btc.model;

import java.io.Serializable;

/**
 * Author: Sergey42
 * Date: 29.03.14 18:30
 */
public class LayerInfo implements Serializable {

    private static final long serialVersionUID = -1483295413300588929L;

    public int neuronCnt;
    public ActivationFunctionType functionType;
    public double coefficient;

    public LayerInfo(int neuronCnt, ActivationFunctionType functionType, double coefficient) {
        this.neuronCnt = neuronCnt;
        this.functionType = functionType;
        this.coefficient = coefficient;
    }

    public Object get(int index) {
        return index == 0 ? neuronCnt : index == 1 ? functionType : coefficient;
    }

    public void set(int index, Object value) {
        if (index == 0) {
            neuronCnt = Integer.valueOf((String) value);
        } else if (index == 1) {
            functionType = (ActivationFunctionType) value;
        } else {
            coefficient = Double.valueOf((String) value);
        }
    }
}
