package hec.ensembleview.charts;

import hec.ensembleview.DefaultSettings;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public class EnsembleDataTablePanel extends JPanel {
    private final DefaultTableModel tableModel;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public EnsembleDataTablePanel() {
        setLayout(new BorderLayout());
        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(tableModel);
        table.setFont(DefaultSettings.setSegoeFontText());
        table.getTableHeader().setFont(DefaultSettings.setSegoeFontTitle());
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setCellSelectionEnabled(true);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    int row = table.rowAtPoint(e.getPoint());
                    int col = table.columnAtPoint(e.getPoint());
                    if (row >= 0 && col >= 0) {
                        Object value = table.getValueAt(row, col);
                        if (value != null) {
                            StringSelection selection = new StringSelection(value.toString());
                            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
                        }
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void setTimeSeriesData(ZonedDateTime[] dates, float[][] memberValues, Map<String, float[]> metricValues) {
        tableModel.setRowCount(0);
        tableModel.setColumnCount(0);

        // Snapshot metric entries in a stable order once — used for both columns and row data
        List<Map.Entry<String, float[]>> metricEntries = metricValues != null
                ? new ArrayList<>(metricValues.entrySet())
                : Collections.emptyList();

        tableModel.addColumn("Date/Time");
        if (memberValues != null) {
            for (int i = 0; i < memberValues.length; i++) {
                tableModel.addColumn("Member " + (i + 1));
            }
        }
        for (Map.Entry<String, float[]> entry : metricEntries) {
            tableModel.addColumn(entry.getKey());
        }

        int rowCount = dates != null ? dates.length : 0;
        for (int r = 0; r < rowCount; r++) {
            Object[] row = new Object[tableModel.getColumnCount()];
            row[0] = dates[r].format(DATE_FORMAT);
            int col = 1;
            if (memberValues != null) {
                for (float[] member : memberValues) {
                    row[col++] = r < member.length ? member[r] : "";
                }
            }
            for (Map.Entry<String, float[]> entry : metricEntries) {
                float[] vals = entry.getValue();
                row[col++] = r < vals.length ? vals[r] : "";
            }
            tableModel.addRow(row);
        }
    }

    public void setScatterData(Map<String, float[]> metricValues) {
        tableModel.setRowCount(0);
        tableModel.setColumnCount(0);

        if (metricValues == null || metricValues.isEmpty()) {
            return;
        }

        tableModel.addColumn("Ensemble");
        for (String metricName : metricValues.keySet()) {
            tableModel.addColumn(metricName);
        }

        int maxRows = 0;
        for (float[] vals : metricValues.values()) {
            maxRows = Math.max(maxRows, vals.length);
        }

        for (int r = 0; r < maxRows; r++) {
            Object[] row = new Object[tableModel.getColumnCount()];
            row[0] = r + 1;
            int col = 1;
            for (float[] vals : metricValues.values()) {
                row[col++] = r < vals.length ? vals[r] : "";
            }
            tableModel.addRow(row);
        }
    }

    public void setProbabilityData(Map<String, Map<Float, Float>> probValues) {
        tableModel.setRowCount(0);
        tableModel.setColumnCount(0);

        if (probValues == null || probValues.isEmpty()) {
            return;
        }

        tableModel.addColumn("Probability");
        for (String metricName : probValues.keySet()) {
            tableModel.addColumn(metricName);
        }

        TreeSet<Float> allProbs = new TreeSet<>();
        for (Map<Float, Float> probMap : probValues.values()) {
            allProbs.addAll(probMap.keySet());
        }

        for (Float prob : allProbs) {
            Object[] row = new Object[tableModel.getColumnCount()];
            row[0] = prob;
            int col = 1;
            for (Map<Float, Float> probMap : probValues.values()) {
                Float val = probMap.get(prob);
                row[col++] = val != null ? val : "";
            }
            tableModel.addRow(row);
        }
    }

    public void clearData() {
        tableModel.setRowCount(0);
        tableModel.setColumnCount(0);
    }
}