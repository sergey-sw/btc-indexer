package com.ssau.btc.model;

import com.ssau.btc.sys.Messages;

/**
 * Author: Sergey42
 * Date: 11.04.14 18:53
 */
public enum NetState {

    NEW("newNetState"),
    TRAINED("trainedNetState");

    String id;

    NetState(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return Messages.get(id);
    }
}
