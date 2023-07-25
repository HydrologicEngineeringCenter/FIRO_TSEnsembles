package hec.ensembleview.viewpanels;

import hec.ensembleview.DatabaseHandlerService;
import hec.ensembleview.DefaultSettings;
import hec.ensembleview.charts.ChartType;
import hec.ensembleview.controllers.ComputePanelController;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StatEnsembleComputePanelView extends JPanel implements ItemListener, ActionListener, PropertyChangeListener {
    private static final Logger logger = Logger.getLogger(StatEnsembleComputePanelView.class.getName());
    private JCheckBox percentiles;
    private JCheckBox cumVol;
    private JTextField percentileText;
    private JTextField cumVolText;
    private final transient ComputePanelController computePanelController;
    private final List<JCheckBox> statisticsList = new ArrayList<>();
    private final List<JTextField> textFieldList = new ArrayList<>();
    private static final String PERCENT_TEXT = "PERCENTILES";
    private static final String VOLUME = "CUMULATIVE VOLUME";

    public StatEnsembleComputePanelView(ComputePanelController computePanelController) {
        this.computePanelController = computePanelController;
        DatabaseHandlerService.getInstance().addDatabaseChangeListener(this);
        initCheckBoxStat();
        initTextBoxStat();
        addCheckboxListeners(statisticsList);
        addTextBoxListeners(textFieldList);
        setupStatsPanel();
        setDefaultFont(statisticsList);
        setVisible(true);
    }

    private void setDefaultFont(List<JCheckBox> list) {
        for(JCheckBox checkBox : list) {
            checkBox.setFont(DefaultSettings.setSegoeFontText());
        }
    }

    private void initCheckBoxStat() {  // Create standard checkboxes
        JCheckBox min = createCheckBox("Min");
        addCheckBoxList(min);
        add(min);
        min.setEnabled(true);

        JCheckBox max = createCheckBox("Max");
        addCheckBoxList(max);
        add(max);
        max.setEnabled(true);

        JCheckBox avg = createCheckBox("Average");
        addCheckBoxList(avg);
        add(avg);
        avg.setEnabled(true);

        JCheckBox stdDev = createCheckBox("StandardDeviation");
        addCheckBoxList(stdDev);
        add(stdDev);
        stdDev.setEnabled(true);

        JCheckBox total = createCheckBox("Total");
        addCheckBoxList(total);
        add(total);
        total.setEnabled(true);
    }

    private void initTextBoxStat() {
        this.percentiles = createCheckBox("Percentiles");
        addCheckBoxList(this.percentiles);
        this.percentiles.setEnabled(true);

        this.percentileText = createTextBox("Percentiles");
        addTextFieldList(this.percentileText);
        this.percentileText.setEnabled(false);
        this.percentileText.setEditable(false);
        groupCheckboxTextfield(percentiles, percentileText);

        this.cumVol = createCheckBox("Cumulative Volume");
        addCheckBoxList(cumVol);
        cumVol.setName("NDayComputable");
        this.cumVol.setEnabled(true);

        this.cumVolText = createTextBox("Cumulative Volume");
        addTextFieldList(this.cumVolText);
        this.cumVolText.setEnabled(false);
        this.cumVolText.setEditable(false);
        groupCheckboxTextfield(cumVol, cumVolText);
    }

    private JCheckBox createCheckBox(String text) {  // create new checkbox
        JCheckBox checkBox = new JCheckBox();
        checkBox.setText(text);
        checkBox.setName(text);
        return checkBox;
    }

    private JTextField createTextBox(String text) {
        JTextField textField = new JTextField();
        textField.setName(text);
        return textField;
    }

    private void setupStatsPanel() {
        setLayout(new GridLayout(3, 1));
        Border graylineBorder = BorderFactory.createLineBorder(Color.LIGHT_GRAY);
        setBorder(BorderFactory.createTitledBorder(graylineBorder, "Statistics", TitledBorder.LEFT, TitledBorder.TOP));
        ((TitledBorder) getBorder()).setTitleFont(DefaultSettings.setSegoeFontTitle());
    }

    public void resetCheckboxes() {
        for(JCheckBox checkBox : statisticsList) {
            checkBox.setSelected(false);
        }
    }

    private void groupCheckboxTextfield(JCheckBox checkBox, JTextField textField) {
        JPanel group = new JPanel();
        group.setLayout(new GridLayout());
        group.add(checkBox);
        group.add(textField);
        add(group);
    }

    private void addCheckBoxList(JCheckBox checkBox) {  // list of existing checkboxes in panel
        statisticsList.add(checkBox);
    }

    private void addTextFieldList(JTextField textField) {
        textFieldList.add(textField);
    }

    private void addCheckboxListeners(List<JCheckBox> list) {  // add checkbox Listeners
        for(JCheckBox checkBox : list) {
            checkBox.addItemListener(this);
        }
    }

    private void addTextBoxListeners(List<JTextField> list) {
        for(JTextField textField : list) {
            textField.addActionListener(this);
        }
    }

    private void setTextFieldAction(JTextField textField, boolean isEnable) {
        textField.setEditable(isEnable);
        textField.setEnabled(isEnable);
    }

    private boolean isTextBoxAvailable(JTextField textField) {
        switch (textField.getName().toUpperCase()) {
            case PERCENT_TEXT:
                if(percentiles.isSelected() && !textField.getText().isEmpty()) {
                    return true;
                }
                break;
            case VOLUME:
                if(cumVol.isSelected() && !textField.getText().isEmpty()) {
                    return true;
                }
                break;
            default:
                return false;
        }
        return false;
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        try {
            if(e.getStateChange() == ItemEvent.SELECTED) {
                JCheckBox source = (JCheckBox) e.getSource();
                String checkboxName = source.getText();

                switch (checkboxName.toUpperCase()) {
                    case PERCENT_TEXT:
                        setTextFieldAction(this.percentileText, true);
                        if(this.percentileText.getText().isEmpty())
                            break;
                        break;
                    case VOLUME:
                        setTextFieldAction(this.cumVolText, true);
                        if(this.cumVolText.getText().isEmpty())
                            break;
                        break;
                    default:
                        computePanelController.setCheckedStatistics(checkboxName, ChartType.SCATTERPLOT);
                        computePanelController.initiateEnsembleCompute(ChartType.SCATTERPLOT);
                        break;
                }
            } else {
                JCheckBox source = (JCheckBox) e.getSource();
                String checkboxName = source.getText();
                switch (checkboxName.toUpperCase()) {
                    case PERCENT_TEXT:
                        setTextFieldAction(this.percentileText, false);
                        this.percentileText.setText("");
                        break;
                    case VOLUME:
                        setTextFieldAction(this.cumVolText, false);
                        this.cumVolText.setText("");
                        break;
                    default:
                        break;
                }
                computePanelController.setRemovedStatistics(checkboxName, ChartType.SCATTERPLOT);
                computePanelController.initiateEnsembleCompute(ChartType.SCATTERPLOT);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.log(Level.SEVERE, "Error in Checkbox selection");
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JTextField textField = (JTextField) e.getSource();
        if(isTextBoxAvailable(textField)) {
            computePanelController.getTextFieldValues(textField);
            try {
                computePanelController.setCheckedStatistics(textField.getName(), ChartType.SCATTERPLOT);
                computePanelController.initiateEnsembleCompute(ChartType.SCATTERPLOT);
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Error in Text Field. Text input cannot be understood or parsed");
            }
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getSource() instanceof DatabaseHandlerService) {
            resetCheckboxes();
        }
    }
}
