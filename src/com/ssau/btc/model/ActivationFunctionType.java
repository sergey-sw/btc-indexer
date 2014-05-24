package com.ssau.btc.model;

import com.ssau.btc.App;
import com.ssau.btc.messages.Messages;

/**
 * Author: Sergey42
 * Date: 14.02.14 21:45
 */
public enum ActivationFunctionType {
    R_SIGMOID,
    C_SIGMOID,
    H_TANGENT,
    SINUS,
    COS;

    @Override
    public String toString() {
        return App.context.getBean(Messages.class).getMessage(name());
    }
}
