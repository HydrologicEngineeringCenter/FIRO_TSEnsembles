package hec.ensembleview.tabs;

import hec.ensemble.stats.Statistics;
import hec.ensembleview.StatComputationHelper;
import hec.ensembleview.mappings.StatisticUIType;
import hec.ensembleview.mappings.SingleValueComboBoxMap;
import hec.ensembleview.mappings.SingleValueSummaryType;
import hec.ensembleview.mappings.StatisticsUITypeMap;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class SingleValueSummaryTab extends JPanel {
    private JPanel leftPanel;
    private JPanel rightPanel;

    private JComboBox<String> summaryTypeComboBox;
    private JComboBox<Statistics> statComboBox1;
    private JComboBox<Statistics> statComboBox2;
    private JTextField textField1;
    private JTextField textField2;

    private JPanel buttonPanel;
    private JButton computeButton;
    private JButton cleanButton;

    private JTextArea outputArea;
    private final transient StatComputationHelper statComputationHelper = new StatComputationHelper();

    public SingleValueSummaryTab() {
        initializeUI();
        organizeUI();
        setSummaryTypeComboBox();
        setActionListeners();
    }

    private Statistics getFirstStat()
    {
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
        for (SingleValueSummaryType type : SingleValueComboBoxMap.getSummaryComboBoxMap().keySet()) {
            if (SingleValueComboBoxMap.getSummaryComboBoxMap().get(type) == summaryTypeComboBox.getSelectedItem())
                return type;
        }
        return null;
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
        summaryTypeComboBox.addActionListener(e -> {
            String s = (String)summaryTypeComboBox.getSelectedItem();
            SingleValueSummaryType type = null;
            for (SingleValueSummaryType t : SingleValueComboBoxMap.summaryComboBoxMap.keySet()) {
                if (Objects.equals(SingleValueComboBoxMap.summaryComboBoxMap.get(t), s)) {
                    type = t;
                    break;
                }
            }
            setupStatComboBoxes(type);

        });

        statComboBox1.addActionListener(e ->
                textField1.setEditable(StatisticsUITypeMap.map.get((Statistics) (statComboBox1.getSelectedItem())) != StatisticUIType.CHECKBOX));

        statComboBox2.addActionListener(e ->
                textField2.setEditable(StatisticsUITypeMap.map.get((Statistics) (statComboBox2.getSelectedItem())) != StatisticUIType.CHECKBOX));

        cleanButton.addActionListener(e -> outputArea.setText(""));

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
            float value = statComputationHelper.computeTwoStepComputable(getFirstStat(), getFirstTextFieldValue(),
                    getSecondStat(), getSecondTextFieldValue(),
                    getSummaryType() == SingleValueSummaryType.COMPUTEACROSSENSEMBLES ||
                            getSummaryType() == SingleValueSummaryType.COMPUTECUMULATIVE);

            tryShowingOutput(value);
        });
    }

    private void organizeUI() {
        setLayout(new GridLayout(2,1));

        add(leftPanel);
        add(rightPanel);

        GroupLayout layout = new GroupLayout(leftPanel);
        leftPanel.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                    .addGroup(layout.createParallelGroup()
                        .addComponent(summaryTypeComboBox)
                        .addComponent(statComboBox1)
                        .addComponent(statComboBox2))
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                .addComponent(textField1)
                                .addComponent(textField2)
                                .addComponent(computeButton))
        );

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(summaryTypeComboBox)).addContainerGap().addGap(50)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(statComboBox1)
                                .addComponent(textField1)).addContainerGap().addGap(50)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(statComboBox2)
                                .addComponent(textField2))
                        .addComponent(computeButton)
        );
        BorderLayout rightArea = new BorderLayout(10,10);

        rightPanel.setLayout(rightArea);

        rightPanel.add(outputArea, BorderLayout.CENTER);
        rightPanel.add(cleanButton, BorderLayout.SOUTH);

    }

    private void setSummaryTypeComboBox() {
        for (String option : SingleValueComboBoxMap.summaryComboBoxMap.values())
            summaryTypeComboBox.addItem(option);

        summaryTypeComboBox.setSelectedItem(null);
    }

    private void setupStatComboBoxes(SingleValueSummaryType option) {
            setupStatComboBox1(option);
            setupStatComboBox2(option);
    }

    private void setupStatComboBox1(SingleValueSummaryType option) {
        statComboBox1.removeAllItems();
        for (Statistics stat : SingleValueComboBoxMap.summaryStatisticsMap.get(option).get(0)) {
            statComboBox1.addItem(stat);
        }
    }

    private void setupStatComboBox2(SingleValueSummaryType option) {
        statComboBox2.removeAllItems();
        for (Statistics stat : SingleValueComboBoxMap.summaryStatisticsMap.get(option).get(1)) {
            statComboBox2.addItem(stat);
        }
    }

    private void initializeUI() {
        leftPanel = new JPanel();
        rightPanel = new JPanel();

        summaryTypeComboBox = new JComboBox<>();
        statComboBox1 = new JComboBox<>();
        statComboBox2 = new JComboBox<>();

        textField1 = new JTextField();
        textField2 = new JTextField();

        buttonPanel = new JPanel();
        computeButton = new JButton("Compute");
        cleanButton = new JButton("Clean");

        outputArea = new JTextArea();
        outputArea.setLineWrap(true);

    }

    private void tryShowingOutput(float result) {
        if (getSummaryType() == SingleValueSummaryType.COMPUTEACROSSENSEMBLES) {
            writeLn(String.join(" ", "Computing",
                    getFirstStatString(), "across all ensemble members for each time-step,",
                    "then computing", getSecondStatString(), "across all time-steps",
                    "=", Float.toString(result)));
        } else if (getSummaryType() == SingleValueSummaryType.COMPUTEACROSSTIME) {
            writeLn(String.join(" ", "Computing",
                    getFirstStatString(), "for each ensemble across all time-steps,",
                    "then computing", getSecondStatString(), "across all ensemble members",
                    "=", Float.toString(result)));
        } else if (getSummaryType() == SingleValueSummaryType.COMPUTECUMULATIVE) {
            writeLn(String.join(" ", "Computing", getFirstStatString() + ",",
                    "then computing", getSecondStatString(), "across all ensemble members =", Float.toString(result)));
        }
    }
}
