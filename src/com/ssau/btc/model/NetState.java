package com.ssau.btc.model;

import com.ssau.btc.App;
import com.ssau.btc.messages.Messages;

/**
 * Author: Sergey42
 * Date: 11.04.14 18:53
 */
public enum NetState {

    NEW("newNetState", 0),
    DATA_INITED("dataInitedState", 10),
    TRAINED("trainedNetState", 20);

    String id;
    int number;

    NetState(String id, int number) {
        this.id = id;
        this.number = number;
    }

    @Override
    public String toString() {
        return App.context.getBean(Messages.class).getMessage(id);
    }

    public boolean isLater(NetState state) {
        return this.number > state.number;
    }
}
