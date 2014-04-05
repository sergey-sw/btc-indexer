package com.ssau.btc.gui;

import com.ssau.btc.sys.Messages;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.IOException;

/**
 * AppFrameComponentLevel
 * <p/>
 * Author: Sergey42
 * Date: 01.04.14 20:37
 */
public class AppFrameCL extends JFrame {

    protected static final int MARGIN = 15;

    protected JTabbedPane jTabbedPane;

    public AppFrameCL() {
        initBase();
        initLocation();
    }

    private void initBase() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        try {
            setIconImage(ImageIO.read(ClassLoader.getSystemResource("com/ssau/btc/resources/btc.png")));
        } catch (IOException ex) {
            System.out.println("IO exception in icon load");
        }
    }

    private void initLocation() {
        setTitle(Messages.get("title"));
        setSize(getToolkit().getScreenSize());
    }
}
