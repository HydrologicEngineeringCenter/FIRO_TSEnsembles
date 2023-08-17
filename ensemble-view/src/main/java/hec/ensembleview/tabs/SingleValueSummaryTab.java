package hec.ensembleview.tabs;

import hec.ensemble.stats.Statistics;
import hec.ensembleview.DatabaseHandlerService;
import hec.ensembleview.DefaultSettings;
import hec.ensembleview.StatComputationHelper;
import hec.ensembleview.mappings.SingleValueComboBoxMap;
import hec.ensembleview.mappings.SingleValueSummaryType;
import hec.ensembleview.mappings.StatisticUIType;
import hec.ensembleview.mappings.StatisticsUITypeMap;
import hec.ensembleview.viewpanels.SingleValueDataTransformView;
import hec.metrics.MetricCollection;
import hec.metrics.MetricCollectionTimeSeries;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Objects;

public class SingleValueSummaryTab extends JPanel {
    private JPanel topPanel;
    private JPanel bottomPanel;
    private SingleValueDataTransformView singleValueDataTransformView;
    private SingleValueSummaryType selectedSummaryType = SingleValueSummaryType.COMPUTEACROSSENSEMBLES;
    private JComboBox<Statistics> statComboBox1;
    private JComboBox<Statistics> statComboBox2;
    private JLabel label1;
    private JLabel label2;
    private JTextField textField1;
    private JTextField textField2;
    private JButton computeButton;
    private JButton clearButton;
    private JTextArea outputArea;
    private String units;
    private final transient StatComputationHelper statComputationHelper = new StatComputationHelper();

    public SingleValueSummaryTab() {
        initializeUI();
        organizeUI();

        initiateSingleValueComboBoxListener();
    }

    private void initiateSingleValueComboBoxListener() {
        singleValueDataTransformView.setSingleValueDataViewListener(summaryType -> {
            if(computeButton.getActionListeners() != null) {
                removeActionListener();
            }
            removeActionListener();
            selectedSummaryType = summaryType;
            setActionListeners();
        });
    }

    private void removeActionListener() {
        for(ActionListener listener : computeButton.getActionListeners()) {
            computeButton.removeActionListener(listener);
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
                assert vals != null;
                r = String.format("%.2f%% %s", vals[0] * 100, stat);
            } else if (stat == Statistics.MAXAVERAGEDURATION || stat == Statistics.MAXACCUMDURATION) {
                assert vals != null;
                r = String.format("%d hour %s", (int)vals[0], stat);
            }
            else {
                r = stat.toString();
            }
        } else {
            if (stat == Statistics.CUMULATIVE) {
                assert vals != null;
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
                assert vals != null;
                r = String.format("%.2f%% %s", vals[0] * 100, stat);
            } else if (stat == Statistics.MAXAVERAGEDURATION || stat == Statistics.MAXACCUMDURATION) {
                assert vals != null;
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
            return null;

        String textValues = textField1.getText();

        if (Objects.equals(textValues, ""))
            return null;

        String[] textValuesParse = textValues.trim().split("[,:;]");
        float[] floatValuesParse = new float[textValuesParse.length];
        for(int i = 0; i < textValuesParse.length; i++) {
            floatValuesParse[i] = Float.parseFloat(textValuesParse[i]);
        }
        return floatValuesParse;
    }

    private float[] getSecondTextFieldValue() {
        if (StatisticsUITypeMap.map.get((Statistics)statComboBox2.getSelectedItem()) != StatisticUIType.TEXTBOX)
            return null;

        String textValues = textField2.getText();

        if (Objects.equals(textValues, ""))
            return null;

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

        statComboBox1.addActionListener(e ->
                textField1.setEditable(StatisticsUITypeMap.map.get((Statistics) (statComboBox1.getSelectedItem())) != StatisticUIType.CHECKBOX));

        statComboBox2.addActionListener(e ->
                textField2.setEditable(StatisticsUITypeMap.map.get((Statistics) (statComboBox2.getSelectedItem())) != StatisticUIType.CHECKBOX));

        clearButton.addActionListener(e -> outputArea.setText(""));

        statComboBox1.addActionListener(e -> {
            if(statComboBox1.getSelectedItem() == Statistics.MAXACCUMDURATION || statComboBox1.getSelectedItem() == Statistics.MAXAVERAGEDURATION) {
                textField1.setToolTipText("Enter value in Hours");
            } else if (statComboBox1.getSelectedItem() == Statistics.PERCENTILES) {
                textField1.setToolTipText("Enter percentile as Decimal");
            } else if (statComboBox1.getSelectedItem() == Statistics.CUMULATIVE) {
                textField1.setToolTipText("Enter value in Days");
            } else {
                textField1.setToolTipText(null);
            }
        });

        statComboBox2.addActionListener(e -> {
            if (statComboBox2.getSelectedItem() == Statistics.PERCENTILES) {
                textField2.setToolTipText("Enter percentile as Decimal");
            } else {
                textField2.setToolTipText(null);
            }
        });

        computeButton.addActionListener(e -> {
            MetricCollectionTimeSeries value = statComputationHelper.computeTwoStepComputable(getFirstStat(), getFirstTextFieldValue(),
                    getSecondStat(), getSecondTextFieldValue(),
                    getSummaryType() == SingleValueSummaryType.COMPUTEACROSSENSEMBLES ||
                            getSummaryType() == SingleValueSummaryType.COMPUTECUMULATIVE);
            getValuesFromMetricCollectionTimeSeries(value);

            tryShowingOutput(getValuesFromMetricCollectionTimeSeries(value));
        });
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

        // setting borders for top and bottom panels
        Border grayLine = BorderFactory.createLineBorder(Color.LIGHT_GRAY);
        topPanel.setBorder((BorderFactory.createTitledBorder(grayLine, "Compute Selection", TitledBorder.LEFT, TitledBorder.TOP)));
        ((TitledBorder )topPanel.getBorder()).setTitleFont(DefaultSettings.setSegoeFontTitle());

        bottomPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        // topPanel contains the comboBoxes, textfields, and buttons.
        // bottomPanel contains the text area.
        add(topPanel, BorderLayout.PAGE_START);
        add(bottomPanel, BorderLayout.CENTER);

        // creating panel to hold comboBoxes, textfields, and buttons and assigning to topPanel
        JPanel selectionPanel = new JPanel();
        selectionPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        topPanel.setLayout(new BorderLayout());
        topPanel.add(selectionPanel, BorderLayout.PAGE_START);

        singleValueDataTransformView = new SingleValueDataTransformView();
        topPanel.add(singleValueDataTransformView, BorderLayout.SOUTH);

        // organizing comboboxes, textfields, and buttons with GridBagLayout
        selectionPanel.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();

        Dimension comboDim = new Dimension();
        comboDim.width = 200;
        comboDim.height = 30;
        statComboBox1.setPreferredSize(comboDim);
        statComboBox2.setPreferredSize(comboDim);

        gc.gridx = 0;
        gc.gridy = 0;
        gc.weightx = .1;

        gc.anchor = GridBagConstraints.LINE_END;
        selectionPanel.add(label1, gc);

        gc.gridx = 1;
        gc.gridy = 0;

        gc.anchor = GridBagConstraints.LINE_START;
        selectionPanel.add(statComboBox1, gc);

        gc.gridx = 2;
        gc.gridy = 0;

        gc.anchor = GridBagConstraints.LINE_START;
        selectionPanel.add(textField1, gc);

        gc.gridx = 0;
        gc.gridy = 1;

        gc.anchor = GridBagConstraints.LINE_END;
        selectionPanel.add(label2, gc);

        gc.gridx = 1;
        gc.gridy = 1;

        gc.anchor = GridBagConstraints.LINE_START;
        selectionPanel.add(statComboBox2, gc);

        gc.gridx = 2;
        gc.gridy = 1;

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

        BorderLayout rightArea = new BorderLayout(10,10);
        bottomPanel.setLayout(rightArea);

        JScrollPane scrollPane = new JScrollPane(outputArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        bottomPanel.add(scrollPane, BorderLayout.CENTER);
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
        textField2 = new JTextField(10);

        computeButton = new JButton("Compute");
        clearButton = new JButton("Clear");

        outputArea = new JTextArea();
        outputArea.setLineWrap(true);
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
}
