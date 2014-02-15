package gui;

import model.ActivationFunctionType;
import sys.Messages;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Author: Sergey42
 * Date: 14.02.14 21:29
 */
public class AppFrame extends JFrame {

    public static AppFrame current;

    public AppFrame() {
        current = this;
        initBase();
        initLocation();
        initComponents();
    }

    private void initBase() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        try {
            setIconImage(ImageIO.read(ClassLoader.getSystemResource("resources/btc.png")));
        } catch (IOException ex) {
            System.out.println("Who cares");
        }
    }

    private void initLocation() {
        setTitle(Messages.get("title"));
        setSize(getToolkit().getScreenSize());
    }

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

        add(jTabbedPane);
    }

    private void initSettingTab() {
        SettingsTableModel tableModel = new SettingsTableModel();
        tableModel.items.addAll(Config.getDefaultStructure());


        JTable jTable = new JTable(tableModel);
        jTable.setSize(400, 100);

        jTable.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(new JTextField()));
        jTable.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(new JTextField()));

        TableColumn functionColumn = jTable.getColumnModel().getColumn(1);
        JComboBox<ActivationFunctionType> box = new JComboBox<>();
        for (ActivationFunctionType type : ActivationFunctionType.values()) {
            box.addItem(type);
        }
        functionColumn.setCellEditor(new DefaultCellEditor(box));

        jTable.setRowHeight(30);

        jTabbedPane.addTab(Messages.get("settingTab"), jTable);
    }

    private JTabbedPane jTabbedPane;

    private static class SettingsTableModel implements TableModel {

        String[] headers;
        Class<?>[] classes;
        public java.util.List<TableItem> items = new ArrayList<>();

        public SettingsTableModel() {
            headers = new String[]
                    {Messages.get("neuronLabel"), Messages.get("functionLabel"), Messages.get("functionCoefficientLabel")};
            classes = new Class[]{int.class, ActivationFunctionType.class, double.class};
        }

        @Override
        public int getRowCount() {
            return 3;
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

        @Override
        public void addTableModelListener(TableModelListener l) {
        }

        @Override
        public void removeTableModelListener(TableModelListener l) {
        }
    }

    static class TableItem {
        int neuronCnt;
        ActivationFunctionType functionType;
        double coefficient;

        TableItem(int neuronCnt, ActivationFunctionType functionType, double coefficient) {
            this.neuronCnt = neuronCnt;
            this.functionType = functionType;
            this.coefficient = coefficient;
        }

        Object get(int index) {
            return index == 0 ? neuronCnt : index == 1 ? functionType : coefficient;
        }

        void set(int index, Object value) {
            if (index == 0) {
                neuronCnt = Integer.valueOf((String) value);
            } else if (index == 1) {
                functionType = (ActivationFunctionType) value;
            } else {
                coefficient = Double.valueOf((String) value);
            }
        }
    }
}
