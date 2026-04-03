package hec.ensembleview.tabs;

import hec.ensemble.Ensemble;
import hec.ensemble.EnsembleTimeSeries;
import hec.ensemble.stats.*;
import hec.ensembleview.DatabaseHandlerService;
import hec.ensembleview.DefaultSettings;
import hec.ensembleview.PlotStatisticsForChartType;
import hec.ensembleview.StatComputationHelper;
import hec.ensembleview.charts.EnsembleChartAcrossTime;
import hec.ensembleview.controllers.SingleValueDataViewListener;
import hec.ensembleview.mappings.SingleValueComboBoxMap;
import hec.ensembleview.mappings.SingleValueSummaryType;
import hec.ensembleview.mappings.StatisticUIType;
import hec.ensembleview.mappings.StatisticsUITypeMap;
import hec.ensembleview.viewpanels.SingleValueDataTransformView;
import hec.gfx2d.G2dPanel;
import hec.metrics.MetricCollectionTimeSeries;
import hec.metrics.MetricCollection;
import hec.metrics.MetricTypes;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SingleValueSummaryTab extends JPanel {
    private JPanel topPanel;
    private JPanel bottomPanel;
    private SingleValueDataTransformView singleValueDataTransformView;
    private SingleValueSummaryType selectedSummaryType = SingleValueSummaryType.COMPUTEACROSSENSEMBLES;
    private JComboBox<Statistics> statComboBox1;
    private JComboBox<Statistics> statComboBox2;
    private String computeOrder1;
    private String computeOrder2;
    private JLabel label1;
    private JLabel label2;
    private JTextField textField1;
    private JTextField textField2;
    private JButton computeButton;
    private JButton findButton;
    private JButton clearButton;
    private JTextArea outputArea;
    private String units;
    private final transient StatComputationHelper statComputationHelper = new StatComputationHelper();

    // Chart/text toggle
    private JToggleButton toggleButton;
    private final CardLayout bottomCardLayout = new CardLayout();
    private JPanel chartContainerPanel;
    private transient EnsembleChartAcrossTime currentChart;
    private G2dPanel currentChartPanel;
    private boolean showingChart = false;
    private static final String TEXT_CARD = "text";
    private static final String CHART_CARD = "chart";

    public SingleValueSummaryTab() {
        initializeUI();
        organizeUI();
        initiateSingleValueComboBoxListener();
    }

    private void initiateSingleValueComboBoxListener() {
        singleValueDataTransformView.setSingleValueDataViewListener(new SingleValueDataViewListener() {
            @Override
            public void initiateComboBoxSelection(SingleValueSummaryType summaryType) {
                removeActionListener();
                selectedSummaryType = summaryType;
                setActionListeners();
            }

            @Override
            public void setComputeOrderLabel(String computeOrder) {
                if(computeOrder.equalsIgnoreCase("Across Time")) {
                    computeOrder1 = computeOrder;
                    computeOrder2 = "Across Ensembles";
                } else {
                    computeOrder1 = computeOrder;
                    computeOrder2 = "Across Time";
                }
                setLabel();
            }
        });
    }

    private void setLabel() {
        label1.setText("1. " + computeOrder1);
        label2.setText("2. " + computeOrder2);
    }

    private void removeActionListener() {
        for(ActionListener listener : computeButton.getActionListeners()) {
            computeButton.removeActionListener(listener);
        }
        for(ActionListener listener : findButton.getActionListeners()) {
            findButton.removeActionListener(listener);
        }
    }

    private Statistics getFirstStat() {
        return (Statistics)statComboBox1.getSelectedItem();
    }

    private String getFirstStatString() {
        String r;
        Statistics stat = getFirstStat();
        float[] vals = getFirstTextFieldValue();
        if (StatisticsUITypeMap.map.get(stat) == StatisticUIType.TEXTBOX) {
            if (stat == Statistics.PERCENTILES) {
                r = String.format("%.2f%% %s", vals[0] * 100, stat);
            } else if (stat == Statistics.MAXAVERAGEDURATION || stat == Statistics.MAXACCUMDURATION) {
                r = String.format("%d hour %s", (int)vals[0], stat);
            }
            else {
                r = stat.toString();
            }
        } else {
            if (stat == Statistics.CUMULATIVE) {
                r = String.format("%s value on day %d", stat, (int)vals[0]);
            } else
                r = stat.toString();
        }
        return r;
    }

    private String getSecondStatString() {
        String r;
        Statistics stat = getSecondStat();
        float[] vals = getSecondTextFieldValue();
        if (StatisticsUITypeMap.map.get(stat) == StatisticUIType.TEXTBOX) {
            if (stat == Statistics.PERCENTILES) {
                r = String.format("%.2f%% %s", vals[0] * 100, stat);
            } else if (stat == Statistics.MAXAVERAGEDURATION || stat == Statistics.MAXACCUMDURATION) {
                r = String.format("%d hour %s", (int)vals[0], stat);
            }
            else {
                r = stat.toString();
            }
        } else {
            r = stat.toString();
        }
        return r;
    }

    private Statistics getSecondStat()
    {
        return (Statistics)statComboBox2.getSelectedItem();
    }

    private SingleValueSummaryType getSummaryType() {
        return selectedSummaryType;
    }

    private float[] getFirstTextFieldValue() {
        if (StatisticsUITypeMap.map.get(statComboBox1.getSelectedItem()) == StatisticUIType.CHECKBOX)
            return new float[0];
        String textValues = textField1.getText();
        if (Objects.equals(textValues, ""))
            return new float[0];

        String[] textValuesParse = textValues.trim().split("[,:;]");
        float[] floatValuesParse = new float[textValuesParse.length];
        for(int i = 0; i < textValuesParse.length; i++) {
            floatValuesParse[i] = Float.parseFloat(textValuesParse[i]);
        }
        return floatValuesParse;
    }

    private float[] getSecondTextFieldValue() {
        if (StatisticsUITypeMap.map.get(statComboBox2.getSelectedItem()) != StatisticUIType.TEXTBOX)
            return new float[0];
        String textValues = textField2.getText();

        if (Objects.equals(textValues, ""))
            return new float[0];
        String[] textValuesParse = textValues.trim().split("[,:;]");
        float[] floatValuesParse = new float[textValuesParse.length];
        for(int i = 0; i < textValuesParse.length; i++) {
            floatValuesParse[i] = Float.parseFloat(textValuesParse[i]);
        }
        return floatValuesParse;
    }

    private void writeLn(String output) {
        outputArea.append("\n" + output);
    }


    private void setActionListeners() {
        setupStatComboBoxes(selectedSummaryType);

        statComboBox1.addActionListener(e -> {
            setTextboxEditable(textField1, statComboBox1);
            setTextFieldToolTip(textField1, statComboBox1);
            updateFindButtonState();
        });

        statComboBox2.addActionListener(e -> {
            setTextboxEditable(textField2, statComboBox2);
            setTextFieldToolTip(textField2, statComboBox2);
            updateFindButtonState();
        });

        clearButton.addActionListener(e -> {
            outputArea.setText("");
            if (showingChart) {
                buildChart();
            }
        });

        computeButton.addActionListener(e -> {
            try {
                MetricCollectionTimeSeries value = statComputationHelper.computeTwoStepComputable(
                        DatabaseHandlerService.getInstance().getEnsembleTimeSeries(),
                        getFirstStat(), getFirstTextFieldValue(),
                        getSecondStat(), getSecondTextFieldValue(),
                        getSummaryType() == SingleValueSummaryType.COMPUTEACROSSENSEMBLES ||
                                getSummaryType() == SingleValueSummaryType.COMPUTECUMULATIVE);
                getValuesFromMetricCollectionTimeSeries(value);
                tryShowingOutput(getValuesFromMetricCollectionTimeSeries(value));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        findButton.addActionListener(e -> findEnsemble());

        updateFindButtonState();
    }

    private void updateFindButtonState() {
        Statistics stat1 = getFirstStat();
        Statistics stat2 = getSecondStat();
        boolean isCumulative = stat1 == Statistics.CUMULATIVE;
        boolean enabled = isCumulative &&
                (stat2 == Statistics.PERCENTILES || stat2 == Statistics.MIN ||
                        stat2 == Statistics.MAX || stat2 == Statistics.AVERAGE);
        findButton.setEnabled(enabled);
        toggleButton.setEnabled(isCumulative);

        if (isCumulative) {
            if (showingChart) {
                toggleButton.setToolTipText("Switch to text view");
            } else {
                toggleButton.setToolTipText("Switch to chart view");
            }
        } else {
            toggleButton.setToolTipText("Select Across Time and Cumulative in Step 1 to enable chart view");
        }

        // Switch back to text view when cumulative is deselected
        if (!isCumulative && showingChart) {
            showingChart = false;
            toggleButton.setSelected(false);
            toggleButton.setIcon(new ChartIcon());
            bottomCardLayout.show(bottomPanel, TEXT_CARD);
            findButton.setVisible(false);

        }
    }

    private void findEnsemble() {
        try {
            DatabaseHandlerService db = DatabaseHandlerService.getInstance();
            Ensemble ensemble = db.getEnsemble();
            if (ensemble == null) {
                JOptionPane.showMessageDialog(this, "No ensemble data loaded.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Statistics stat1 = getFirstStat();
            float[] dayValues = getFirstTextFieldValue();
            Statistics stat2 = getSecondStat();
            float[] stat2Values = getSecondTextFieldValue();

            if (stat1 == Statistics.CUMULATIVE && dayValues.length == 0) {
                JOptionPane.showMessageDialog(this, "Enter a day value for Cumulative.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // For percentiles, each value is a separate percentile to find
            // For min/max/average, stat2Values is empty (no text field needed)
            float[] step2Entries;
            if (stat2 == Statistics.PERCENTILES) {
                if (stat2Values.length == 0) {
                    JOptionPane.showMessageDialog(this, "Enter a percentile value.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                step2Entries = stat2Values;
            } else {
                step2Entries = new float[]{0}; // single placeholder for min/max/avg
            }

            List<Integer> highlightedMembers = new java.util.ArrayList<>();

            for (float day : dayValues) {
                float[] singleDay = {day};
                // Compute cumulative volume per ensemble member for this day
                Computable step1 = new NDayMultiComputable(new CumulativeComputable(), singleDay);
                float[] memberVolumes = ensemble.iterateForTracesAcrossTime(step1);

                for (float entry : step2Entries) {
                    // Build the step 2 computable for this specific value
                    float[] singleVal = (stat2 == Statistics.PERCENTILES) ? new float[]{entry} : new float[0];
                    Computable step2 = StatComputationHelper.getComputable(stat2, singleVal);
                    NearestIndexComputable finder = new NearestIndexComputable(step2);
                    int memberIndex = finder.compute(memberVolumes);
                    highlightedMembers.add(memberIndex);

                    // Compute the target value for display
                    float targetValue = step2.compute(memberVolumes);
                    String dayLabel = String.format("%.0f-day Cumulative", day);
                    String statLabel;
                    if (stat2 == Statistics.PERCENTILES) {
                        statLabel = String.format("%.2f%% Percentile", entry * 100);
                    } else {
                        statLabel = stat2.toString();
                    }
                    writeLn("Found: Member " + (memberIndex + 1) + " is closest to " +
                            statLabel + " of " + dayLabel +
                            " (value = " + targetValue + ")");
                }
            }

            // Build chart and highlight all found members
            buildChart();
            if (currentChart != null) {
                currentChart.highlightMembers(highlightedMembers);
                if (currentChartPanel != null) {
                    currentChartPanel.repaint();
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Find Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void buildChart() {
        chartContainerPanel.removeAll();

        DatabaseHandlerService db = DatabaseHandlerService.getInstance();
        if (db.getDbHandlerRid() == null || db.getDbHandlerZdt() == null) return;

        // Mirror the Time Series tab: use cumulative ensemble if cumulative mode is active
        Ensemble ensemble;
        if (db.isCumulativeView()) {
            EnsembleTimeSeries cumulativeEts = db.getCumulativeEnsembleTimeSeries();
            Ensemble cumEnsemble = (cumulativeEts != null) ? cumulativeEts.getEnsemble(db.getDbHandlerZdt()) : null;
            ensemble = (cumEnsemble != null) ? cumEnsemble : db.getEnsemble();
        } else {
            ensemble = db.getEnsemble();
        }
        if (ensemble == null) return;

        currentChart = new EnsembleChartAcrossTime();
        currentChart.setYLabel(db.getDbHandlerRid().parameter + " (" + ensemble.getUnits() + ")");

        // Add ensemble member lines
        PlotStatisticsForChartType.addLineMembersToChart(currentChart, ensemble.getValues(), ensemble.startDateTime());

        // Generate the chart panel (only Y1 data at this point)
        currentChartPanel = currentChart.generateChart();
        chartContainerPanel.add(currentChartPanel, BorderLayout.CENTER);

        // Add any metrics computed in the Time Series tab
        addMetricsToChart(currentChart, ensemble, db);

        chartContainerPanel.revalidate();
        chartContainerPanel.repaint();
    }

    private void addMetricsToChart(EnsembleChartAcrossTime chart, Ensemble ensemble, DatabaseHandlerService db) {
        Map<Statistics, MetricCollectionTimeSeries> metricMap = db.getMetricCollectionTimeSeriesMap();
        ZonedDateTime[] dates = ensemble.startDateTime();

        for (Map.Entry<Statistics, MetricCollectionTimeSeries> entry : metricMap.entrySet()) {
            MetricCollectionTimeSeries mcts = entry.getValue();
            if (mcts.getMetricType() == MetricTypes.TIMESERIES_OF_ARRAY) {
                db.setResidentMetricCollectionTimeSeries(mcts);
                String stats = db.getResidentMetricStatisticsList();
                String[] statCollection = stats.split("\\|");
                float[][] vals = db.getResidentMetricStatisticsValues();
                for (int i = 0; i < vals.length; i++) {
                    PlotStatisticsForChartType.addMetricStatisticsToTimePlot(chart, statCollection[i], vals[i], dates);
                }
            }
        }
    }

    private void setTextFieldToolTip(JTextField textField, JComboBox<Statistics> comboBox) {
        if(comboBox.getSelectedItem() == Statistics.MAXACCUMDURATION || comboBox.getSelectedItem() == Statistics.MAXAVERAGEDURATION) {
            textField.setToolTipText("Enter value in Hours");
        } else if (comboBox.getSelectedItem() == Statistics.PERCENTILES) {
            textField.setToolTipText("Enter percentile as Decimal");
        } else if (comboBox.getSelectedItem() == Statistics.CUMULATIVE) {
            textField.setToolTipText("Enter value in Days");
        } else {
            textField.setToolTipText(null);
        }
    }

    private void setTextboxEditable(JTextField textField, JComboBox<Statistics> statComboBox) {
        textField.setEditable(StatisticsUITypeMap.map.get((statComboBox.getSelectedItem())) != StatisticUIType.CHECKBOX);
        textField.setText("");
    }

    private float getValuesFromMetricCollectionTimeSeries(MetricCollectionTimeSeries mcts) {
        DatabaseHandlerService db = DatabaseHandlerService.getInstance();
        MetricCollection metricCollection = mcts.getMetricCollection(db.getDbHandlerZdt());
        setSingleValueUnits(metricCollection);

        float[][] metricCollectionValues = metricCollection.getValues();
        return metricCollectionValues[0][0];
    }

    private void setSingleValueUnits(MetricCollection metricCollection) {
        units = metricCollection.getUnits();
    }

    private void organizeUI() {
        setLayout(new BorderLayout());

        // --- Top Panel: Compute Selection + Data View Row ---
        topPanel.setLayout(new BorderLayout());

        // Compute Selection Panel
        JPanel selectionPanel = new JPanel();
        Border grayLine = BorderFactory.createLineBorder(Color.LIGHT_GRAY);
        selectionPanel.setBorder((BorderFactory.createTitledBorder(grayLine, "Compute Selection", TitledBorder.LEFT, TitledBorder.TOP)));
        ((TitledBorder)selectionPanel.getBorder()).setTitleFont(DefaultSettings.setSegoeFontTitle());

        selectionPanel.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();

        Dimension comboDim = new Dimension();
        comboDim.width = 150;
        comboDim.height = 25;
        statComboBox1.setPreferredSize(comboDim);
        statComboBox2.setPreferredSize(comboDim);

        gc.gridx = 0;
        gc.gridy = 0;
        gc.weightx = .1;
        gc.anchor = GridBagConstraints.LINE_START;
        selectionPanel.add(label1, gc);

        gc.gridx = 1;
        gc.gridy = 0;
        gc.insets = new Insets(5, 0, 5, 0);
        gc.anchor = GridBagConstraints.LINE_START;
        selectionPanel.add(statComboBox1, gc);

        gc.gridx = 2;
        gc.gridy = 0;
        gc.weightx = 1;
        gc.anchor = GridBagConstraints.LINE_START;
        selectionPanel.add(textField1, gc);

        gc.gridx = 0;
        gc.gridy = 1;
        gc.weightx = .1;
        gc.anchor = GridBagConstraints.LINE_START;
        selectionPanel.add(label2, gc);

        gc.gridx = 1;
        gc.gridy = 1;
        gc.anchor = GridBagConstraints.LINE_START;
        selectionPanel.add(statComboBox2, gc);

        gc.gridx = 2;
        gc.gridy = 1;
        gc.weightx = 1;
        gc.anchor = GridBagConstraints.LINE_START;
        selectionPanel.add(textField2, gc);

        JPanel buttonGroup = new JPanel();
        buttonGroup.add(computeButton);
        buttonGroup.add(clearButton);
        gc.anchor = GridBagConstraints.FIRST_LINE_END;
        gc.gridx = 5;
        gc.gridy = 1;
        gc.weightx = .8;
        gc.weighty = .2;
        selectionPanel.add(buttonGroup, gc);

        topPanel.add(selectionPanel, BorderLayout.PAGE_START);

        // Data View Row: toggle button + Compute Order panel + Find button
        singleValueDataTransformView = new SingleValueDataTransformView();
        JPanel dataViewRow = new JPanel(new BorderLayout());
        JPanel togglePanel = new JPanel(new GridBagLayout());
        togglePanel.add(toggleButton);
        dataViewRow.add(togglePanel, BorderLayout.WEST);
        dataViewRow.add(singleValueDataTransformView, BorderLayout.CENTER);

        JPanel findPanel = new JPanel(new GridBagLayout());
        findPanel.add(findButton);
        findButton.setVisible(false);
        dataViewRow.add(findPanel, BorderLayout.EAST);

        topPanel.add(dataViewRow, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.PAGE_START);

        // --- Bottom Panel: CardLayout with text area and chart ---
        bottomPanel.setLayout(bottomCardLayout);
        bottomPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        JScrollPane scrollPane = new JScrollPane(outputArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        bottomPanel.add(scrollPane, TEXT_CARD);

        chartContainerPanel = new JPanel(new BorderLayout());
        bottomPanel.add(chartContainerPanel, CHART_CARD);

        bottomCardLayout.show(bottomPanel, TEXT_CARD);

        add(bottomPanel, BorderLayout.CENTER);
    }

    private void setupStatComboBoxes(SingleValueSummaryType option) {
            setupStatComboBox1(option);
            setupStatComboBox2(option);
    }

    private void setupStatComboBox1(SingleValueSummaryType option) {
        statComboBox1.removeAllItems();
        for (Statistics stat : SingleValueComboBoxMap.getSummaryStatisticsMap().get(option).get(0)) {
            statComboBox1.addItem(stat);
        }
    }

    private void setupStatComboBox2(SingleValueSummaryType option) {
        statComboBox2.removeAllItems();
        for (Statistics stat : SingleValueComboBoxMap.getSummaryStatisticsMap().get(option).get(1)) {
            statComboBox2.addItem(stat);
        }
    }

    private void initializeUI() {
        topPanel = new JPanel();
        bottomPanel = new JPanel();

        label1 = new JLabel("1.");
        label2 = new JLabel("2.");

        statComboBox1 = new JComboBox<>();
        statComboBox2 = new JComboBox<>();

        textField1 = new JTextField(10);
        textField1.setEditable(false);
        textField2 = new JTextField(10);
        textField2.setEditable(false);

        computeButton = new JButton("Compute");
        findButton = new JButton("Find");
        findButton.setEnabled(false);
        findButton.setToolTipText("Find the ensemble member closest to the computed value");
        clearButton = new JButton("Clear");

        outputArea = new JTextArea();
        outputArea.setLineWrap(true);

        toggleButton = createToggleButton();
        toggleButton.setEnabled(false);
        toggleButton.setToolTipText("Select Across Time and Cumulative in Step 1 to enable chart view");
    }

    private JToggleButton createToggleButton() {
        JToggleButton btn = new JToggleButton(new ChartIcon());
        btn.setToolTipText("Toggle Text/Chart View");
        btn.setPreferredSize(new Dimension(28, 28));
        btn.setFocusPainted(false);
        btn.setMargin(new Insets(2, 2, 2, 2));
        btn.addActionListener(e -> {
            showingChart = btn.isSelected();
            if (showingChart) {
                btn.setIcon(new TextIcon());
                btn.setToolTipText("Switch to text view");
                buildChart();
                bottomCardLayout.show(bottomPanel, CHART_CARD);
                findButton.setVisible(true);

                updateFindButtonState();
            } else {
                btn.setIcon(new ChartIcon());
                btn.setToolTipText("Switch to chart view");
                bottomCardLayout.show(bottomPanel, TEXT_CARD);
                findButton.setVisible(false);
    
            }
        });
        return btn;
    }

    private void tryShowingOutput(float result) {
        if (getSummaryType() == SingleValueSummaryType.COMPUTEACROSSENSEMBLES) {
            writeLn(String.join(" ", "Computing",
                    getFirstStatString(), "across all ensemble members for each time-step,",
                    "then computing", getSecondStatString(), "across all time-steps",
                    "=", Float.toString(result), "(", units, ")"));
        } else if (getSummaryType() == SingleValueSummaryType.COMPUTEACROSSTIME) {
            writeLn(String.join(" ", "Computing",
                    getFirstStatString(), "for each ensemble across all time-steps,",
                    "then computing", getSecondStatString(), "across all ensemble members",
                    "=", Float.toString(result), "(", units, ")"));
        } else if (getSummaryType() == SingleValueSummaryType.COMPUTECUMULATIVE) {
            writeLn(String.join(" ", "Computing", getFirstStatString() + ",",
                    "then computing", getSecondStatString(), "across all ensemble members =", Float.toString(result), "(", units, ")"));
        }
    }

    // --- Icon inner classes ---

    static class ChartIcon implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.DARK_GRAY);
            int w = getIconWidth();
            int h = getIconHeight();
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawLine(x + 2, y + 1, x + 2, y + h - 1);
            g2.drawLine(x + 2, y + h - 1, x + w - 1, y + h - 1);
            g2.setColor(new Color(51, 153, 255));
            g2.setStroke(new BasicStroke(1.5f));
            int[] xPoints = {x + 3, x + 6, x + 9, x + 12, x + 15};
            int[] yPoints = {y + 10, y + 5, y + 8, y + 3, y + 6};
            g2.drawPolyline(xPoints, yPoints, 5);
            g2.dispose();
        }

        @Override
        public int getIconWidth() { return 16; }

        @Override
        public int getIconHeight() { return 16; }
    }

    static class TextIcon implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(Color.DARK_GRAY);
            g2.setStroke(new BasicStroke(1.5f));
            int w = getIconWidth();
            // Draw horizontal lines representing text
            for (int i = 0; i < 4; i++) {
                int ly = y + 2 + i * 4;
                int lineWidth = (i == 2) ? w - 4 : w; // Shorter third line for visual variety
                g2.drawLine(x, ly, x + lineWidth, ly);
            }
            g2.dispose();
        }

        @Override
        public int getIconWidth() { return 16; }

        @Override
        public int getIconHeight() { return 16; }
    }
}