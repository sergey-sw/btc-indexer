package com.ssau.btc.model;

/**
 * Author: Sergey42
 * Date: 29.03.14 18:30
 */
public class LayerInfo {

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
