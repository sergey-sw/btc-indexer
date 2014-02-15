package gui;

import sys.Messages;

/**
 * Author: Sergey42
 * Date: 14.02.14 21:28
 */
public class App {

    public static void main(String[] args) {
        Messages.init("ru");

        AppFrame appFrame = new AppFrame();
        appFrame.setVisible(true);
    }
}
