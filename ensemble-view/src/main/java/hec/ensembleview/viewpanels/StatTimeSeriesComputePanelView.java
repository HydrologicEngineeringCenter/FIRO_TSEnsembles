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

public class StatTimeSeriesComputePanelView extends JPanel implements ItemListener, ActionListener, PropertyChangeListener {
    private static final Logger logger = Logger.getLogger(StatTimeSeriesComputePanelView.class.getName());
    private JCheckBox percentiles;
    private JTextField percentileValues;
    private final transient ComputePanelController computePanelController;
    private final List<JCheckBox> statisticsList = new ArrayList<>();

    public StatTimeSeriesComputePanelView(ComputePanelController computePanelController) {
        this.computePanelController = computePanelController;
        DatabaseHandlerService.getInstance().addDatabaseChangeListener(this);
        initCheckBoxes();
        addCheckboxListeners(statisticsList);
        addTextBoxListeners(percentileValues);
        setupStatsPanel();
        setDefaultFont(statisticsList);
        setVisible(true);
    }

    private void setDefaultFont(List<JCheckBox> list) {
        for(JCheckBox checkBox : list) {
            checkBox.setFont(DefaultSettings.setSegoeFontText());
        }
    }

    private void initCheckBoxes() {  // Create standard checkboxes
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

        this.percentiles = createCheckBox("Percentiles");
        addCheckBoxList(this.percentiles);
        this.percentiles.setEnabled(true);

        this.percentileValues = createTextBox();
        this.percentileValues.setEnabled(false);
        this.percentileValues.setEditable(false);
        groupCheckboxTextfield(this.percentiles, this.percentileValues);
    }

    private JCheckBox createCheckBox(String text) {  // create new checkbox
        JCheckBox checkBox = new JCheckBox();
        checkBox.setText(text);
        checkBox.setName(text);
        return checkBox;
    }

    private JTextField createTextBox() {
        JTextField textField = new JTextField();
        textField.setName("Percentiles");
        return textField;
    }

    private void setupStatsPanel() {
        setLayout(new GridLayout(2, 1));
        Border graylineBorder = BorderFactory.createLineBorder(Color.LIGHT_GRAY);
        setBorder(BorderFactory.createTitledBorder(graylineBorder, "Statistics", TitledBorder.LEFT, TitledBorder.TOP));
        ((TitledBorder) getBorder()).setTitleFont(DefaultSettings.setSegoeFontTitle());
    }

    private void resetCheckboxes() {
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

    private void addCheckboxListeners(List<JCheckBox> list) {  // add checkbox Listeners
        for(JCheckBox checkBox : list) {
            checkBox.addItemListener(this);
        }
    }

    private void addTextBoxListeners(JTextField percentileValues) {
        percentileValues.addActionListener( this);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        try {
            if(e.getStateChange() == ItemEvent.SELECTED) {
                JCheckBox source = (JCheckBox) e.getSource();
                String checkboxName = source.getName();
                if(checkboxName.equalsIgnoreCase("percentiles")) {
                    percentileValues.setEnabled(true);
                    percentileValues.setEditable(true);
                    if(percentileValues.getText().isEmpty()){
                        return;
                    }
                }
                computePanelController.setCheckedStatistics(checkboxName, ChartType.TIMEPLOT);
                computePanelController.initiateTimeSeriesCompute(ChartType.TIMEPLOT);
            } else {
                JCheckBox source = (JCheckBox) e.getSource();
                String checkboxName = source.getName();
                if(checkboxName.equalsIgnoreCase("percentiles")) {
                    percentileValues.setEnabled(false);
                    percentileValues.setEditable(false);
                    percentileValues.setText("");
                }
                computePanelController.setRemovedStatistics(checkboxName, ChartType.TIMEPLOT);
                computePanelController.initiateTimeSeriesCompute(ChartType.TIMEPLOT);
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error in Checkbox selection");
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == percentileValues && percentiles.isSelected()) {
            if(percentileValues.getText().isEmpty()) {
                return;
            }
            computePanelController.getTextFieldValues(percentileValues);
            try {
                computePanelController.setCheckedStatistics(percentiles.getName(), ChartType.TIMEPLOT);
                computePanelController.initiateTimeSeriesCompute(ChartType.TIMEPLOT);
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
