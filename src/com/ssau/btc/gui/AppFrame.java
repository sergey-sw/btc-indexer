package com.ssau.btc.gui;

import com.ssau.btc.model.*;
import com.ssau.btc.sys.Messages;
import com.ssau.btc.sys.WebDataLoader;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Sergey42
 * Date: 14.02.14 21:29
 */
public class AppFrame extends AppFrameCL {

    public AppFrame() {
        super();
        initComponents();
    }

    protected JButton addLayerBtn;
    protected JButton removeLayerBtn;
    protected JButton standardLayersBtn;
    protected SettingsTableModel structureTableModel;
    protected JTable structureTable;
    protected JPanel settingsPanel;

    private void initComponents() {
        jTabbedPane = new JTabbedPane();

        initSettingTab();

        JButton exitBtn2 = new JButton("Exit");
        exitBtn2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        jTabbedPane.addTab(Messages.get("forecastTab"), exitBtn2);

        initChartTab();
        initMistakeTab();

        add(jTabbedPane);
    }

    private void initSettingTab() {
        settingsPanel = new JPanel();
        FlowLayout settingsPanelLayout = new FlowLayout(FlowLayout.LEFT);
        settingsPanelLayout.setVgap(MARGIN);
        settingsPanelLayout.setHgap(MARGIN);
        settingsPanel.setLayout(settingsPanelLayout);

        JPanel tablePanelOuter = new JPanel();
        BoxLayout tableLayout = new BoxLayout(tablePanelOuter, BoxLayout.Y_AXIS);
        tablePanelOuter.setLayout(tableLayout);
        tablePanelOuter.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        JPanel tablePanelInner = new JPanel();
        FlowLayout tablePanelInnerLayout = new FlowLayout(FlowLayout.LEFT);
        tablePanelInner.setLayout(tablePanelInnerLayout);

        JLabel tableLabel = new JLabel(Messages.get("tableLabelCaption"));
        tablePanelOuter.add(tableLabel);
        tablePanelOuter.add(Box.createVerticalStrut(10));
        tablePanelOuter.add(Box.createHorizontalStrut(10));

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addLayerBtn = new JButton(Messages.get("addLayer"));
        addLayerBtn.addActionListener(new AddLayerHandler());
        removeLayerBtn = new JButton(Messages.get("removeLayer"));
        removeLayerBtn.addActionListener(new RemoveLayerHandler());
        removeLayerBtn.setEnabled(false);
        standardLayersBtn = new JButton(Messages.get("standardLayers"));
        standardLayersBtn.addActionListener(new StandardLayerHandler());
        buttonsPanel.add(addLayerBtn);
        buttonsPanel.add(removeLayerBtn);
        buttonsPanel.add(standardLayersBtn);
        tablePanelOuter.add(buttonsPanel);

        structureTableModel = new SettingsTableModel();
        for (LayerInfo layerInfo : Config.getDefaultStructure()) {
            structureTableModel.addItem(layerInfo);
        }

        structureTable = new JTable(structureTableModel);
        structureTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        structureTable.setRowHeight(30);
        structureTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        structureTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                int size = structureTableModel.items.size();
                boolean selected = false;
                for (int i = 1; i < size; i++) {
                    selected |= structureTable.getSelectionModel().isSelectedIndex(i);
                }
                removeLayerBtn.setEnabled(selected);
            }
        });

        TableColumn column0 = structureTable.getColumnModel().getColumn(0);
        column0.setMinWidth(200);
        column0.setCellEditor(new DefaultCellEditor(new JTextField()));

        TableColumn column2 = structureTable.getColumnModel().getColumn(2);
        column2.setMinWidth(200);
        column2.setCellEditor(new DefaultCellEditor(new JTextField()));

        TableColumn column1 = structureTable.getColumnModel().getColumn(1);
        column1.setMinWidth(200);
        JComboBox<ActivationFunctionType> box = new JComboBox<>();
        for (ActivationFunctionType type : ActivationFunctionType.values()) {
            box.addItem(type);
        }
        column1.setCellEditor(new DefaultCellEditor(box));

        tablePanelInner.add(structureTable);
        tablePanelOuter.add(tablePanelInner);

        settingsPanel.add(tablePanelOuter);

        jTabbedPane.addTab(Messages.get("settingTab"), settingsPanel);
    }

    private void initChartTab() {
        JPanel chartJPanel = new JPanel(new FlowLayout());
        jTabbedPane.addTab("Chart", chartJPanel);

        WebDataLoader webDataLoader = new WebDataLoader();
        List<IndexSnapshot> indexSnapshots = webDataLoader.loadCoinDeskIndexes("2014-01-01", "2014-03-01", SnapshotMode.CLOSING_PRICE);
        TimeSeriesCollection timeDataSet = ChartHelper.createTimeDataSet(indexSnapshots);
        JFreeChart chart = ChartHelper.createTimeChart(timeDataSet);

        final ChartPanel chartPanel = new ChartPanel(chart);
        Dimension screenSize = getToolkit().getScreenSize();
        Dimension chartSize = new Dimension(Double.valueOf(screenSize.width * 0.9).intValue(), Double.valueOf(screenSize.height * 0.9).intValue());
        chartPanel.setPreferredSize(chartSize);

        chartJPanel.add(chartPanel);
    }

    private void initMistakeTab() {
        JPanel chartJPanel = new JPanel(new FlowLayout());
        jTabbedPane.addTab("Mistakes", chartJPanel);

        NetworkAPI network = NetworkCreator.buildDefault();

        WebDataLoader dataLoader = new WebDataLoader();
        List<IndexSnapshot> indexSnapshots = dataLoader.loadCoinDeskIndexes("2014-01-01", "2014-03-01", SnapshotMode.CLOSING_PRICE);

        double[] data = IndexSnapshotUtils.parseClosingPrice(indexSnapshots);
        network.initInputData(data, Interval.DAY);

        network.setValue("speedRate", 0.7);
        network.setValue("teachCycleCount", 50);
        network.setValue("studyLength", data.length);

        network.teach();

        double[] adpeh = (double[]) network.getValue("averageDiffPerEraHistory");
        XYDataset xyDataset = ChartHelper.createXYDataSet(adpeh);
        JFreeChart chart = ChartHelper.createDoublesChart(xyDataset);

        final ChartPanel chartPanel = new ChartPanel(chart);
        Dimension screenSize = getToolkit().getScreenSize();
        Dimension chartSize = new Dimension(Double.valueOf(screenSize.width * 0.9).intValue(), Double.valueOf(screenSize.height * 0.9).intValue());
        chartPanel.setPreferredSize(chartSize);

        chartJPanel.add(chartPanel);
    }


    private class AddLayerHandler implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            structureTableModel.addItem(Config.createLayerInfo());
            structureTable.repaint();

            addLayerBtn.setEnabled(structureTableModel.items.size() < 5);
        }
    }

    private class RemoveLayerHandler implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            structureTableModel.removeItem(structureTable.getSelectedRow());
            addLayerBtn.setEnabled(structureTableModel.items.size() < 5);
        }
    }

    private class StandardLayerHandler implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            int size = structureTableModel.items.size();
            for (int i = size - 1; i > 0; i--) {
                structureTableModel.removeItem(i);
            }

            List<LayerInfo> layerInfoList = Config.getDefaultStructure();
            for (int i = 1; i < layerInfoList.size(); i++) {
                structureTableModel.addItem(layerInfoList.get(i));
            }

            addLayerBtn.setEnabled(true);
        }
    }

    private class SettingsTableModel extends DefaultTableModel {

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
