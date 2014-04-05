package com.ssau.btc;

import com.intelli.ray.core.Context;
import com.ssau.btc.gui.AppFrame;
import com.ssau.btc.sys.Synchronizer;

/**
 * Author: Sergey42
 * Date: 14.02.14 21:28
 */
public class App {

    public static void main(String[] args) {
        Context context = new Context(new String[]{"btc-indexer"}, "com.ssau.btc");

        Synchronizer synchronizer = context.getBean(Synchronizer.class);
        synchronizer.initTimer();

        AppFrame appFrame = context.getBean(AppFrame.class);
        appFrame.postInit();
        appFrame.setVisible(true);
    }
}
