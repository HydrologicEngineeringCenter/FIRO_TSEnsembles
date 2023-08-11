package hec.ensembleview.viewpanels;

import hec.ensembleview.DatabaseHandlerService;
import hec.ensembleview.controllers.EnsembleArrayComputePanelListener;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.beans.PropertyChangeEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StatEnsembleComputePanelView extends ComputePanelView {
    private static final Logger logger = Logger.getLogger(StatEnsembleComputePanelView.class.getName());
    private JCheckBox percentiles;
    private JCheckBox cumVol;
    private JTextField percentileText;
    private JTextField cumVolText;
    private static final String PERCENT_TEXT = "PERCENTILES";
    private static final String VOLUME = "CUMULATIVE VOLUME";
    private transient EnsembleArrayComputePanelListener ensembleListener;

    public StatEnsembleComputePanelView() {
        super();
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

        JCheckBox total = createCheckBox("Total");
        addCheckBoxList(total);
        add(total);
        total.setEnabled(true);
    }

    @Override
    protected void initTextBoxStat() {
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

    protected boolean isTextBoxAvailable(JTextField textField) {
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

    public void setEnsembleListener(EnsembleArrayComputePanelListener listener) {
        this.ensembleListener = listener;
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
                        this.ensembleListener.setCheckedStatistics(checkboxName);
                        this.ensembleListener.initiateEnsembleCompute();
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
                this.ensembleListener.setRemovedStatistics(checkboxName);
                this.ensembleListener.initiateEnsembleCompute();
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
            try {
                this.ensembleListener.getTextFieldValues(textField);
                this.ensembleListener.setCheckedStatistics(textField.getName());
                this.ensembleListener.initiateEnsembleCompute();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
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
