package com.ssau.btc.gui;

import com.intelli.ray.core.Inject;
import com.intelli.ray.core.ManagedComponent;
import com.ssau.btc.model.*;
import com.ssau.btc.sys.*;
import net.sourceforge.jdatepicker.JDateComponentFactory;
import net.sourceforge.jdatepicker.impl.JDatePickerImpl;
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
import java.util.Calendar;
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

    protected NetworkAPI currentNetwork;

    public void postInit() {
        initComponents();
        threadManager.scheduleTask(new Runnable() {
            @Override
            public void run() {
                currentPrice = currentPriceProvider.getCurrentPrice();
                usdValue.setText(String.format(H1_PATTERN, currentPrice.USD));
                if (prevUSDValue != null) {
                    usdDiffValue.setText(String.format(H1_PATTERN, currentPrice.calcDiff(prevUSDValue)));
                    if (currentPrice.diff.startsWith("+")) {
                        usdDiffValue.setForeground(green);
                    } else {
                        usdDiffValue.setForeground(Color.RED);
                    }
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
        initNetworkTab();
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

        ratesPanel.add(new JLabel());
        ratesPanel.add(new JLabel());
        ratesPanel.add(new JLabel());

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
        BoxLayout chartBoxLayout = new BoxLayout(chartJPanel, BoxLayout.Y_AXIS);
        chartJPanel.setLayout(chartBoxLayout);

        //todo modes
        JPanel buttonsPanel = new JPanel(SIMPLE_FLOW_LAYOUT);
        buttonsPanel.add(dayModeBtn);
        buttonsPanel.add(monthModeBtn);
        buttonsPanel.add(yearModeBtn);
        dayModeBtn.addActionListener(new ModeChangeHandler(ModeChangeHandler.DAY));
        monthModeBtn.addActionListener(new ModeChangeHandler(ModeChangeHandler.MONTH));
        yearModeBtn.addActionListener(new ModeChangeHandler(ModeChangeHandler.YEAR));
        chartJPanel.add(buttonsPanel);

        String to = DateUtils.format(new Date());
        String from = DateUtils.format(DateUtils.calcDate(new Date(), Calendar.HOUR, -24));

        //todo background
        Collection<IndexSnapshot> indexSnapshots = webDataLoader.loadCoinDeskIndexes(from, to, SnapshotMode.CLOSING_PRICE, WebLoaderAPI.HOUR);
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

    protected void initNetworkTab() {
        networkMainPanel = new JPanel(MARGIN_FLOW_LAYOUT);

        JPanel networkVPanel = new JPanel();
        BoxLayout networkPanelBoxLayout = new BoxLayout(networkVPanel, BoxLayout.Y_AXIS);
        networkVPanel.setLayout(networkPanelBoxLayout);
        networkMainPanel.add(networkVPanel);

        JPanel netButtonsPanel = new JPanel(SIMPLE_FLOW_LAYOUT);
        createNetBtn = new JButton(Messages.get("newNet"));
        createNetBtn.addActionListener(new CreateNetButtonHandler());
        netButtonsPanel.add(createNetBtn);
        loadNetBtn = new JButton(Messages.get("loadNet"));
        loadNetBtn.addActionListener(new LoadNetButtonHandler());
        netButtonsPanel.add(loadNetBtn);
        saveNetBtn = new JButton(Messages.get("saveNet"));
        saveNetBtn.addActionListener(new SaveNetButtonHandler());
        saveNetBtn.setEnabled(false);
        netButtonsPanel.add(saveNetBtn);
        networkVPanel.add(netButtonsPanel);

        structureTablePanelOuter = new JPanel(MARGIN_FLOW_LAYOUT);
        structureTablePanelOuter.setVisible(false);
        structureTablePanelOuter.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        JPanel structureTablePanelInner = new JPanel();
        structureTablePanelOuter.add(structureTablePanelInner);
        BoxLayout tablePanelInnerLayout = new BoxLayout(structureTablePanelInner, BoxLayout.Y_AXIS);
        structureTablePanelInner.setLayout(tablePanelInnerLayout);

        JLabel tableLabel = new JLabel(Messages.get("tableLabelCaption"));
        structureTablePanelInner.add(tableLabel);
        structureTablePanelInner.add(Box.createVerticalStrut(10));

        JPanel layerButtonsPanel = new JPanel(SIMPLE_FLOW_LAYOUT);
        addLayerBtn = new JButton(Messages.get("addLayer"));
        addLayerBtn.addActionListener(new AddLayerHandler());
        removeLayerBtn = new JButton(Messages.get("removeLayer"));
        removeLayerBtn.addActionListener(new RemoveLayerHandler());
        removeLayerBtn.setEnabled(false);
        standardLayersBtn = new JButton(Messages.get("standardLayers"));
        standardLayersBtn.addActionListener(new StandardLayerHandler());
        layerButtonsPanel.add(addLayerBtn);
        layerButtonsPanel.add(removeLayerBtn);
        layerButtonsPanel.add(standardLayersBtn);
        structureTablePanelInner.add(layerButtonsPanel);

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

        JPanel structureTableWrapper = new JPanel(SIMPLE_FLOW_LAYOUT);
        structureTableWrapper.add(structureTable);

        structureTablePanelInner.add(structureTableWrapper);
        structureTablePanelInner.add(Box.createVerticalStrut(10));

        netStatePanel = new JPanel(SIMPLE_FLOW_LAYOUT);
        netStatePanel.setVisible(false);

        JLabel netStateCaption = new JLabel(Messages.get("netState"));
        netStatePanel.add(netStateCaption);
        netStateLabel = new JLabel();
        netStatePanel.add(netStateLabel);
        structureTablePanelInner.add(netStatePanel);

        JPanel buildNetButtonPanel = new JPanel(SIMPLE_FLOW_LAYOUT);
        buildNetBtn = new JButton(Messages.get("buildNet"));
        buildNetBtn.addActionListener(new BuildNetButtonHandler());
        buildNetButtonPanel.add(buildNetBtn);
        structureTablePanelInner.add(buildNetButtonPanel);

        teachPanelOuter = new JPanel(MARGIN_FLOW_LAYOUT);
        teachPanelOuter.setVisible(false);
        teachPanelOuter.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        JPanel teachPanelInner = new JPanel();
        BoxLayout teachPanelInnerLayout = new BoxLayout(teachPanelInner, BoxLayout.Y_AXIS);
        teachPanelInner.setLayout(teachPanelInnerLayout);
        teachPanelOuter.add(teachPanelInner);

        JLabel teachPanelLabel = new JLabel(Messages.get("teachPanel"));
        teachPanelInner.add(teachPanelLabel);
        teachPanelInner.add(Box.createVerticalStrut(MARGIN));

        teachPanel = new JPanel(new GridLayout(5, 2, 10, 10));

        JLabel dateFrom = new JLabel(Messages.get("dateFrom"));
        fromDatePicker = (JDatePickerImpl) JDateComponentFactory.createJDatePicker();
        teachPanel.add(dateFrom);
        teachPanel.add(fromDatePicker);

        JLabel dateTill = new JLabel(Messages.get("dateTill"));
        tillDatePicker = (JDatePickerImpl) JDateComponentFactory.createJDatePicker();
        teachPanel.add(dateTill);
        teachPanel.add(tillDatePicker);

        JLabel teachCoeffLabel = new JLabel(Messages.get("teachCoeff"));
        teachCoeffTF = new JTextField(Config.DEFAULT_TEACH_COEFF);
        teachPanel.add(teachCoeffLabel);
        teachPanel.add(teachCoeffTF);

        JLabel eraCntLabel = new JLabel(Messages.get("eraCount"));
        eraCountTF = new JTextField(Config.DEFAULT_ERA_CNT);
        teachPanel.add(eraCntLabel);
        teachPanel.add(eraCountTF);

        teachBtn = new JButton(Messages.get("teachBtn"));
        teachBtn.addActionListener(new TeachNetButtonHandler());
        teachPanel.add(teachBtn);

        teachPanelInner.add(teachPanel);

        forecastPanelOuter = new JPanel(SIMPLE_FLOW_LAYOUT);
        forecastPanelOuter.setVisible(false);
        forecastPanelOuter.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        JPanel forecastPanelInner = new JPanel();
        BoxLayout forecastPanelInnerLayout = new BoxLayout(forecastPanelInner, BoxLayout.Y_AXIS);
        forecastPanelInner.setLayout(forecastPanelInnerLayout);
        forecastPanelOuter.add(forecastPanelInner);

        JLabel forecastLabel = new JLabel(Messages.get("forecastPanel"));
        forecastPanelInner.add(forecastLabel);
        forecastPanelInner.add(Box.createVerticalStrut(MARGIN));

        JPanel forecastParamsPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        forecastPanelInner.add(forecastParamsPanel);

        JLabel forecastDateFrom = new JLabel(Messages.get("dateFrom"));
        forecastDateTF = new JTextField();
        forecastDateTF.setFocusable(false);
        forecastDateTF.setPreferredSize(fromDatePicker.getPreferredSize());
        forecastParamsPanel.add(forecastDateFrom);
        forecastParamsPanel.add(forecastDateTF);

        JLabel forecastSizeLabel = new JLabel(Messages.get("forecastSize"));
        forecastParamsPanel.add(forecastSizeLabel);
        forecastSizeTF = new JTextField(Config.DEFAULT_FORECAST_SIZE);
        forecastParamsPanel.add(forecastSizeTF);

        forecastBtn = new JButton(Messages.get("forecastBtn"));
        forecastBtn.addActionListener(new ForecastButtonHandler());
        forecastParamsPanel.add(forecastBtn);

        networkVPanel.add(structureTablePanelOuter);
        networkVPanel.add(Box.createVerticalStrut(10));
        networkVPanel.add(teachPanelOuter);
        networkVPanel.add(Box.createVerticalStrut(10));
        networkVPanel.add(forecastPanelOuter);

        networkMainPanel.add(networkVPanel);

        jTabbedPane.addTab(Messages.get("settingTab"), networkMainPanel);
    }

    protected void initMistakeTab() {
        JPanel chartJPanel = new JPanel(new FlowLayout());
        jTabbedPane.addTab("Mistakes", chartJPanel);

        NetworkAPI network = NetworkCreator.buildDefault();

        WebDataLoader dataLoader = new WebDataLoader();
        Collection<IndexSnapshot> indexSnapshots = dataLoader.
                loadCoinDeskIndexes("2014-01-01", "2014-03-01", SnapshotMode.CLOSING_PRICE, WebLoaderAPI.DAY);

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

    protected void setStructurePanelEnabled(boolean enabled) {
        structureTable.getSelectionModel().clearSelection();
        structureTable.setEnabled(enabled);
        addLayerBtn.setVisible(enabled);
        removeLayerBtn.setVisible(enabled);
        standardLayersBtn.setVisible(enabled);
        buildNetBtn.setVisible(enabled);
        netStatePanel.setVisible(!enabled);
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

    protected class ModeChangeHandler implements ActionListener {

        int mode;
        public static final int DAY = 0;
        public static final int MONTH = 1;
        public static final int YEAR = 2;

        public ModeChangeHandler(int mode) {
            this.mode = mode;
        }

        @Override
        public void actionPerformed(ActionEvent e) {

        }
    }

    protected class CreateNetButtonHandler implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            standardLayersBtn.doClick();
            structureTablePanelOuter.setVisible(true);
            setStructurePanelEnabled(true);
            teachPanelOuter.setVisible(false);

            saveNetBtn.setEnabled(true);
        }
    }

    protected class SaveNetButtonHandler implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("Not implemented");
        }
    }

    protected class LoadNetButtonHandler implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("Not implemented");
        }
    }

    protected class BuildNetButtonHandler implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (validate()) {
                currentNetwork = NetworkCreator.create(structureTableModel.items);

                setStructurePanelEnabled(false);
                teachPanelOuter.setVisible(true);

                netStateLabel.setText(Messages.get("newNetState"));
            }
        }

        protected boolean validate() {
            int i = 0;
            for (LayerInfo layerInfo : structureTableModel.items) {
                if (layerInfo.functionType == null && i != 0) {
                    showMessage(
                            Messages.get("error"),
                            Messages.format("error.functionTypeIsNull", i + 1),
                            JOptionPane.ERROR_MESSAGE);
                    return false;
                }

                if (layerInfo.neuronCnt < 0 && i != 0) {
                    showMessage(
                            Messages.get("error"),
                            Messages.format("error.negativeNeuronCount", i + 1),
                            JOptionPane.ERROR_MESSAGE);
                    return false;
                }

                if (layerInfo.neuronCnt > Config.MAX_LAYER_NEURON_CNT) {
                    showMessage(
                            Messages.get("error"),
                            Messages.format("error.maxNeuronCount", i + 1, Config.MAX_LAYER_NEURON_CNT),
                            JOptionPane.ERROR_MESSAGE);
                    return false;
                }

                if (layerInfo.coefficient <= -1 || layerInfo.coefficient >= 1) {
                    showMessage(
                            Messages.get("error"),
                            Messages.format("error.invalidActivateCoefficient", i + 1),
                            JOptionPane.ERROR_MESSAGE);
                    return false;
                }

                ++i;
            }

            return true;
        }
    }

    protected class TeachNetButtonHandler implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (validate()) {
                //todo teaching
                Calendar calendar = (Calendar) tillDatePicker.getModel().getValue();
                forecastDateTF.setText(DateUtils.format(calendar.getTime()));

                forecastPanelOuter.setVisible(true);
            }
        }

        protected boolean validate() {
            Calendar from = (Calendar) fromDatePicker.getModel().getValue();
            if (from == null) {
                showMessage(
                        Messages.get("error"),
                        Messages.get("error.nullDateFrom"),
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }

            if (from.getTime().before(DateUtils.getDate(Config.MIN_DATE_FROM))) {
                showMessage(
                        Messages.get("error"),
                        Messages.format("error.minDateFrom", Config.MIN_DATE_FROM),
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }

            Calendar till = (Calendar) tillDatePicker.getModel().getValue();
            if (till == null) {
                showMessage(
                        Messages.get("error"),
                        Messages.get("error.nullDateTill"),
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }

            if (till.getTime().after(new Date())) {
                showMessage(
                        Messages.get("error"),
                        Messages.get("error.maxDateTill"),
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }

            try {
                int eraCnt = Integer.valueOf(eraCountTF.getText());

                if (eraCnt <= 0 || eraCnt > Config.MAX_ERA_COUNT) {
                    showMessage(
                            Messages.get("error"),
                            Messages.format("error.badEraCnt", Config.MAX_ERA_COUNT),
                            JOptionPane.ERROR_MESSAGE);
                    return false;
                }

            } catch (Exception ex) {
                showMessage(
                        Messages.get("error"),
                        Messages.get("error.invalidEraCnt"),
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }

            try {
                double teachCoeff = Double.valueOf(teachCoeffTF.getText());

                if (teachCoeff <= 0 || teachCoeff >= 1) {
                    showMessage(
                            Messages.get("error"),
                            Messages.get("error.badTeachCoeff"),
                            JOptionPane.ERROR_MESSAGE);
                    return false;
                }

            } catch (Exception ex) {
                showMessage(
                        Messages.get("error"),
                        Messages.get("error.invalidTeachCoeff"),
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }

            return true;
        }
    }

    protected class ForecastButtonHandler implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (validate()) {

            }
        }

        protected boolean validate() {
            String forecastSize = forecastSizeTF.getText();
            try {
                int value = Integer.valueOf(forecastSize);

                if (value <= 0) {
                    showMessage(Messages.get("error"), Messages.get("error.badForecastSize"), JOptionPane.ERROR_MESSAGE);
                    return false;
                }

            } catch (Exception ex) {
                showMessage(Messages.get("error"), Messages.get("error.invalidForecastSize"), JOptionPane.ERROR_MESSAGE);
                return false;
            }

            return true;
        }
    }
}
