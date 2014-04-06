package com.ssau.btc.gui;

import com.ssau.btc.model.ActivationFunctionType;
import com.ssau.btc.model.LayerInfo;
import com.ssau.btc.sys.CurrentPriceProvider;
import com.ssau.btc.sys.Messages;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

/**
 * AppFrameComponentLevel
 * <p/>
 * Author: Sergey42
 * Date: 01.04.14 20:37
 */
public class AppFrameCL extends JFrame {

    protected static final int MARGIN = 15;

    protected JTabbedPane jTabbedPane;

    protected JButton addLayerBtn;
    protected JButton removeLayerBtn;
    protected JButton standardLayersBtn;
    protected SettingsTableModel structureTableModel;
    protected JTable structureTable;
    protected JPanel structurePanel;

    protected JLabel usdValue;
    protected JLabel usdDiffValue;
    protected JLabel eurValue;
    protected JLabel eurDiffValue;
    protected JLabel rateTsLabel;

    protected String H1_PATTERN = "<html><h1><b>%s</b></h1></html>";
    protected String H2_PATTERN = "<html><h2><b>%s</b></h2></html>";
    protected String H3_PATTERN = "<html><h3><b>%s</b></h3></html>";

    protected Color green = new Color(34, 139, 34);

    protected static final int HISTORY_DAY_COUNT = 10;
    protected JLabel[] prevDiffLabels = new JLabel[HISTORY_DAY_COUNT];
    protected JLabel[] prevDateLabels = new JLabel[HISTORY_DAY_COUNT];
    protected JLabel[] prevPriceLabels = new JLabel[HISTORY_DAY_COUNT];

    protected CurrentPriceProvider.Price currentPrice;
    protected volatile Double prevUSDValue;

    public AppFrameCL() {
        initBase();
        initLocation();
    }

    protected void initBase() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        try {
            setIconImage(ImageIO.read(ClassLoader.getSystemResource("com/ssau/btc/resources/btc.png")));
        } catch (IOException ex) {
            System.out.println("IO exception in icon load");
        }
    }

    protected void initLocation() {
        setTitle(Messages.get("title"));
        setSize(getToolkit().getScreenSize());
    }

    protected class SettingsTableModel extends DefaultTableModel {

        String[] headers;
        Class<?>[] classes;
        public java.util.List<LayerInfo> items = new ArrayList<>();

        public SettingsTableModel() {
            super();
            headers = new String[]
                    {Messages.get("neuronLabel"), Messages.get("functionLabel"), Messages.get("functionCoefficientLabel")};
            classes = new Class[]{int.class, ActivationFunctionType.class, double.class};
        }

        public void addItem(LayerInfo layerInfo) {
            items.add(layerInfo);
            insertRow(items.size() - 1, new Object[]{layerInfo.neuronCnt, layerInfo.functionType, layerInfo.coefficient});
        }

        public void removeItem(int index) {
            items.remove(index);
            removeRow(index);
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public String getColumnName(int columnIndex) {
            return headers[columnIndex];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return classes[columnIndex];
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return !(rowIndex == 0 && columnIndex != 0);

        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (rowIndex == 0 && columnIndex != 0) {
                return null;
            }

            return items.get(rowIndex).get(columnIndex);
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            items.get(rowIndex).set(columnIndex, aValue);
        }
    }
}
