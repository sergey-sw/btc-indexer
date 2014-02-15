package model;

import sys.Messages;

/**
 * Author: Sergey42
 * Date: 14.02.14 21:45
 */
public enum ActivationFunctionType {
    R_SIGMOID,
    C_SIGMOID,
    H_TANGENT,
    SINUS;

    @Override
    public String toString() {
        return Messages.get(name());
    }
}
