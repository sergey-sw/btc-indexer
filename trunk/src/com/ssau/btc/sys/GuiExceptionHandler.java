package com.ssau.btc.sys;

import com.ssau.btc.App;
import com.ssau.btc.gui.AppFrame;
import com.ssau.btc.messages.Messages;

import javax.swing.*;

/**
 * Author: Sergey42
 * Date: 24.05.14 21:29
 */
public class GuiExceptionHandler {

    public static void handleDbError(Exception e) {
        String url = Config.getDbUrl();
        String user = Config.getDbUser();
        String pass = Config.getDbPass();

        if (App.context != null && App.context.isStarted()) {
            Messages messages = App.context.getBean(Messages.class);

            AppFrame appFrame = App.appFrame;
            if (appFrame != null) {
                appFrame.showMessage(
                        messages.getMessage("error.dbError"),
                        messages.formatMessage("error.dbErrorDescription", url, user, pass),
                        JOptionPane.ERROR_MESSAGE);
            } else {
                throw new RuntimeException("TODO");
            }
        } else {
            JOptionPane.showMessageDialog(new JFrame(), String.format("Can't connect to database.\n" +
                    "URL: %s\n" +
                    "User: %s\n" +
                    "Pass: %s\n" +
                    "Verify your connection settings",
                    url, user, pass), "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    public static void handleCritical(String s) {
        if (App.context != null && App.context.isStarted()) {
            Messages messages = App.context.getBean(Messages.class);
            AppFrame appFrame = App.appFrame;
            if (appFrame != null) {
                appFrame.showMessage(messages.getMessage("error.critical"), s, JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            } else {
                showAndClose(s);
            }
        } else {
            showAndClose(s);
        }
    }

    protected static void showAndClose(String s) {
        JOptionPane.showMessageDialog(new JFrame(), s);
        System.exit(0);
    }
}
