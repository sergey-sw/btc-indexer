package com.ssau.btc;

import com.intelli.ray.core.Context;
import com.ssau.btc.gui.AppFrame;
import com.ssau.btc.messages.Messages;
import com.ssau.btc.sys.Config;
import com.ssau.btc.sys.Synchronizer;

import java.io.IOException;

/**
 * Author: Sergey42
 * Date: 14.02.14 21:28
 */
public class App {

    public static Context context;
    public static AppFrame appFrame;

    public static void main(String[] args) {
        context = new Context(new String[]{"btc-indexer"}, "com.ssau.btc");
        try {
            context.printConfiguredBeans(null);
        } catch (IOException e) {
            System.out.println("Could not print beans");
        }
        Synchronizer synchronizer = context.getBean(Synchronizer.class);
        synchronizer.initTimer();

        Messages messages = context.getBean(Messages.class);
        messages.init(Config.getLocale());

        appFrame = context.getPrototype(AppFrame.NAME);
        appFrame.init();
        appFrame.setVisible(true);
    }
}
