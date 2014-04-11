package com.ssau.btc.model;

import com.ssau.btc.sys.Messages;

import java.io.Serializable;

/**
 * Author: Sergey42
 * Date: 14.02.14 21:45
 */
public enum ActivationFunctionType implements Serializable {
    R_SIGMOID,
    C_SIGMOID,
    H_TANGENT,
    SINUS;

    @Override
    public String toString() {
        return Messages.get(name());
    }
}
