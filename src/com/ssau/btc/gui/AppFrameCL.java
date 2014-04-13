package com.ssau.btc.gui;

import com.ssau.btc.model.ActivationFunctionType;
import com.ssau.btc.model.LayerInfo;
import com.ssau.btc.sys.CurrentPriceProvider;
import com.ssau.btc.sys.Messages;
import net.sourceforge.jdatepicker.impl.JDatePickerImpl;
import org.jfree.data.time.TimeSeriesCollection;

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

    private static final long serialVersionUID = 7185169783868241076L;

    protected static final int MARGIN = 15;

    protected JTabbedPane jTabbedPane;

    protected JButton addLayerBtn;
    protected JButton removeLayerBtn;
    protected JButton standardLayersBtn;
    protected SettingsTableModel structureTableModel;
    protected JTable structureTable;
    protected JPanel networkMainPanel;
    protected JPanel structureTablePanelOuter;

    protected JLabel usdValue;
    protected JLabel usdDiffValue;
    protected JLabel rateTsLabel;

    protected JButton dayModeBtn = new JButton(Messages.get("day"));
    protected JButton monthModeBtn = new JButton(Messages.get("month"));
    protected JButton yearModeBtn = new JButton(Messages.get("year"));

    protected JButton createNetBtn;
    protected JButton loadNetBtn;
    protected JButton saveNetBtn;
    protected JButton buildNetBtn;
    protected JPanel netStatePanel;
    protected JLabel netStateLabel;

    protected JPanel teachPanel;
    protected JPanel teachPanelOuter;
    protected JDatePickerImpl fromDatePicker;
    protected JDatePickerImpl tillDatePicker;
    protected JTextField speedRateTF;
    protected JTextField teachCycleCountTF;
    protected JButton teachBtn;

    protected JPanel forecastPanel;
    protected JPanel forecastPanelOuter;
    protected JTextField forecastSizeTF;
    protected JButton forecastBtn;
    protected JTextField forecastDateTF;

    protected JPanel mistakesPanel;
    protected JPanel valuesPanel;

    protected TimeSeriesCollection networkDataSet;

    protected FlowLayout SIMPLE_FLOW_LAYOUT = new FlowLayout(FlowLayout.LEFT);
    protected FlowLayout MARGIN_FLOW_LAYOUT = new FlowLayout(FlowLayout.LEFT);

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
        MARGIN_FLOW_LAYOUT.setHgap(MARGIN);
        MARGIN_FLOW_LAYOUT.setVgap(MARGIN);

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

    protected void showMessage(String caption, String message, int messageType) {
        JOptionPane.showMessageDialog(this, message, caption, messageType);
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
