package gui;

import sys.DataManager;
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

        DataManager dataManager = new DataManager();
        try {
            dataManager.testSettings();
        } catch (Exception e) {
            System.out.println("Failed to find driver: " + e.getMessage());
        }
    }
}
