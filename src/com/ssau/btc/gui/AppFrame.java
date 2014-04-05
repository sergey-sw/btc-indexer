package com.ssau.btc.gui;

import com.intelli.ray.core.Inject;
import com.intelli.ray.core.ManagedComponent;
import com.ssau.btc.model.*;
import com.ssau.btc.sys.*;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Author: Sergey42
 * Date: 14.02.14 21:29
 */
@ManagedComponent(name = "AppFrame")
public class AppFrame extends AppFrameCL {

    @Inject
    protected CurrentPriceProvider currentPriceProvider;
    @Inject
    protected ThreadManager threadManager;
    @Inject
    protected WebLoaderAPI webDataLoader;

    public void postInit() {
        initComponents();
        threadManager.scheduleTask(new Runnable() {
            @Override
            public void run() {
                CurrentPriceProvider.Price price = currentPriceProvider.getCurrentPrice();
                usdValue.setText(String.format(H1_PATTERN, price.USD));
                eurValue.setText(String.format(H1_PATTERN, price.EUR));

                if (prevUSDValue != null) {
                    usdDiffValue.setText(String.format(H1_PATTERN, price.calcDiff(prevUSDValue)));
                    if (price.diff.startsWith("+")) {
                        usdDiffValue.setForeground(green);
                    } else {
                        usdDiffValue.setForeground(Color.RED);
                    }
                    eurDiffValue.setText(usdDiffValue.getText());
                }
            }
        }, 10, TimeUnit.SECONDS);
        threadManager.scheduleTask(new Runnable() {
            @Override
            public void run() {
                rateTsLabel.setText(String.format(H2_PATTERN, DateUtils.formatTime(new Date())));
            }
        }, 50, TimeUnit.MILLISECONDS);
        threadManager.submitTask(new Runnable() {
            @Override
            public void run() {
                List<CurrentPriceProvider.Price> lastPrices = currentPriceProvider.getLastPrices(HISTORY_DAY_COUNT);
                prevUSDValue = lastPrices.get(0).usdDouble;
                for (int i = 0; i < lastPrices.size(); i++) {
                    CurrentPriceProvider.Price price = lastPrices.get(i);

                    prevDateLabels[i].setText(price.ts);
                    prevPriceLabels[i].setText(price.USD);
                    prevDiffLabels[i].setText(price.diff);

                    if (price.diff.startsWith("+")) {
                        prevDiffLabels[i].setForeground(green);
                    } else {
                        prevDiffLabels[i].setForeground(Color.RED);
                    }
                }
            }
        });
    }

    protected void initComponents() {
        jTabbedPane = new JTabbedPane();

        initInfoTab();
        initStructureTab();
        initChartTab();
        initMistakeTab();

        add(jTabbedPane);
    }

    protected void initInfoTab() {
        FlowLayout infoPanelLayout = new FlowLayout(FlowLayout.LEFT);
        infoPanelLayout.setHgap(10);
        JPanel infoPanel = new JPanel(infoPanelLayout);

        JPanel ratesPanel = new JPanel(new GridLayout(4 + HISTORY_DAY_COUNT, 3, 20, 5));
        ratesPanel.setPreferredSize(new Dimension(350, 450));

        JLabel ratesLabel = new JLabel(String.format(H2_PATTERN, Messages.get("ratesCaption")));
        rateTsLabel = new JLabel();
        ratesPanel.add(ratesLabel);
        ratesPanel.add(rateTsLabel);
        ratesPanel.add(new JLabel(String.format(H1_PATTERN, "+/-")));

        JLabel usdLabel = new JLabel(String.format(H1_PATTERN, "USD"));
        usdValue = new JLabel(String.format(H1_PATTERN, "..."));
        ratesPanel.add(usdLabel);
        ratesPanel.add(usdValue);
        usdDiffValue = new JLabel(String.format(H1_PATTERN, "..."));
        ratesPanel.add(usdDiffValue);

        JLabel eurLabel = new JLabel(String.format(H1_PATTERN, "EUR"));
        eurValue = new JLabel(String.format(H1_PATTERN, "..."));
        ratesPanel.add(eurLabel);
        ratesPanel.add(eurValue);
        eurDiffValue = new JLabel(String.format(H1_PATTERN, "..."));
        ratesPanel.add(eurDiffValue);

        ratesPanel.add(new JLabel(String.format(H3_PATTERN, Messages.get("prevDays"))));
        ratesPanel.add(new JLabel());
        ratesPanel.add(new JLabel());

        for (int i = 0; i < HISTORY_DAY_COUNT; i++) {
            prevDateLabels[i] = new JLabel();
            prevPriceLabels[i] = new JLabel();
            prevDiffLabels[i] = new JLabel();

            ratesPanel.add(prevDateLabels[i]);
            ratesPanel.add(prevPriceLabels[i]);
            ratesPanel.add(prevDiffLabels[i]);
        }

        infoPanel.add(ratesPanel);

        JPanel chartJPanel = new JPanel();
        String to = DateUtils.format(new Date());
        String from = DateUtils.format(DateUtils.calcDate(new Date(), -HISTORY_DAY_COUNT - 1));

        Collection<IndexSnapshot> indexSnapshots = webDataLoader.loadCoinDeskIndexes(from, to, SnapshotMode.CLOSING_PRICE);
        TimeSeriesCollection timeDataSet = ChartHelper.createTimeDataSet(indexSnapshots);
        JFreeChart chart = ChartHelper.createTimeChart(timeDataSet);

        final ChartPanel chartPanel = new ChartPanel(chart);
        Dimension screenSize = getToolkit().getScreenSize();
        Dimension chartSize = new Dimension(
                Double.valueOf(screenSize.width * 0.7).intValue(),
                Double.valueOf(screenSize.height * 0.6).intValue());
        chartPanel.setMaximumSize(chartSize);
        chartJPanel.add(chartPanel);

        infoPanel.add(chartJPanel);

        jTabbedPane.addTab(Messages.get("infoTab"), infoPanel);
    }

    protected void initStructureTab() {
        structurePanel = new JPanel();
        FlowLayout settingsPanelLayout = new FlowLayout(FlowLayout.LEFT);
        settingsPanelLayout.setVgap(MARGIN);
        settingsPanelLayout.setHgap(MARGIN);
        structurePanel.setLayout(settingsPanelLayout);

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

        structurePanel.add(tablePanelOuter);

        jTabbedPane.addTab(Messages.get("settingTab"), structurePanel);
    }

    protected void initChartTab() {
        JPanel chartJPanel = new JPanel(new FlowLayout());
        jTabbedPane.addTab("Chart", chartJPanel);

        String to = DateUtils.format(new Date());
        String from = DateUtils.format(DateUtils.calcDate(new Date(), -HISTORY_DAY_COUNT - 1));

        Collection<IndexSnapshot> indexSnapshots = webDataLoader.loadCoinDeskIndexes(from, to, SnapshotMode.CLOSING_PRICE);
        TimeSeriesCollection timeDataSet = ChartHelper.createTimeDataSet(indexSnapshots);
        JFreeChart chart = ChartHelper.createTimeChart(timeDataSet);

        final ChartPanel chartPanel = new ChartPanel(chart);
        Dimension screenSize = getToolkit().getScreenSize();
        Dimension chartSize = new Dimension(Double.valueOf(screenSize.width * 0.5).intValue(), Double.valueOf(screenSize.height * 0.52).intValue());
        chartPanel.setMaximumSize(chartSize);

        chartJPanel.add(chartPanel);
    }

    protected void initMistakeTab() {
        JPanel chartJPanel = new JPanel(new FlowLayout());
        jTabbedPane.addTab("Mistakes", chartJPanel);

        NetworkAPI network = NetworkCreator.buildDefault();

        WebDataLoader dataLoader = new WebDataLoader();
        Collection<IndexSnapshot> indexSnapshots = dataLoader.loadCoinDeskIndexes("2014-01-01", "2014-03-01", SnapshotMode.CLOSING_PRICE);

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


    protected class AddLayerHandler implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            structureTableModel.addItem(Config.createLayerInfo());
            structureTable.repaint();

            addLayerBtn.setEnabled(structureTableModel.items.size() < 5);
        }
    }

    protected class RemoveLayerHandler implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            structureTableModel.removeItem(structureTable.getSelectedRow());
            addLayerBtn.setEnabled(structureTableModel.items.size() < 5);
        }
    }

    protected class StandardLayerHandler implements ActionListener {

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
}
