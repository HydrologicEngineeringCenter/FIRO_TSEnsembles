package hec.ensembleview.viewpanels;

import hec.ensembleview.DatabaseHandlerService;
import hec.ensembleview.controllers.TimeSeriesComputePanelListener;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.beans.PropertyChangeEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StatTimeSeriesComputePanelView extends ComputePanelView {
    private static final Logger logger = Logger.getLogger(StatTimeSeriesComputePanelView.class.getName());
    private transient TimeSeriesComputePanelListener timeListener;
    private JCheckBox percentiles;
    private JTextField percentileValues;

    public StatTimeSeriesComputePanelView() {
        super();
    }

    public void setTimeListener(TimeSeriesComputePanelListener listener) {
        this.timeListener = listener;
    }

    @Override
    protected void initCheckBoxStat() {  // Create standard checkboxes
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
    }

    @Override
    protected void initTextBoxStat() {
        this.percentileValues = createTextBox("Percentiles");
        this.percentileValues.setToolTipText("Enter Percentile as Decimal");
        addTextFieldList(this.percentileValues);
        this.percentileValues.setEnabled(false);
        this.percentileValues.setEditable(false);
        groupCheckboxTextfield(this.percentiles, this.percentileValues);
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
                timeListener.setCheckedStatistics(checkboxName);
                timeListener.initiateTimeSeriesCompute();
            } else {
                JCheckBox source = (JCheckBox) e.getSource();
                String checkboxName = source.getName();
                if(checkboxName.equalsIgnoreCase("percentiles")) {
                    percentileValues.setEnabled(false);
                    percentileValues.setEditable(false);
                    percentileValues.setText("");
                }
                timeListener.setRemovedStatistics(checkboxName);
                timeListener.initiateTimeSeriesCompute();
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
            try {
                timeListener.getTextFieldValues(percentileValues);
                timeListener.setCheckedStatistics(percentiles.getName());
                timeListener.initiateTimeSeriesCompute();
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Error in Text Field. Text input cannot be understood or parsed");
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getSource() instanceof DatabaseHandlerService) {
            removeCheckBoxListener(statisticsList);
            resetCheckboxes();
            addCheckboxListeners(statisticsList);
        }
    }
}
