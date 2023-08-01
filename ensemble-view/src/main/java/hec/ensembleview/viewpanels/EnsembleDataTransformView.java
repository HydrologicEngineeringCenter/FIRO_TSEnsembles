package hec.ensembleview.viewpanels;

import hec.ensembleview.DatabaseHandlerService;
import hec.ensembleview.DefaultSettings;
import hec.ensembleview.controllers.ProbabilityDataViewListener;
import hec.ensembleview.charts.ChartType;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;

public class EnsembleDataTransformView extends DataTransformView {
    private JRadioButton original;
    private JRadioButton probability;
    private transient ProbabilityDataViewListener probabilityListener;
    public EnsembleDataTransformView() {
        super();
    }

    public void setProbabilityListener(ProbabilityDataViewListener listener) {
        this.probabilityListener = listener;
    }

    @Override
    protected void initiateButtonSelection() {
        if(original.isSelected()) {
            actionPerformed(new ActionEvent(original, ActionEvent.ACTION_PERFORMED, "selected"));
        } else if (probability.isSelected()) {
            actionPerformed(new ActionEvent(probability, ActionEvent.ACTION_PERFORMED, "selected"));
        }
    }

    @Override
    protected void initiateRadioButton() {
        this.original = new JRadioButton();
        this.original.setName("Original");
        this.original.setText("Original");
        this.original.setFont(DefaultSettings.setSegoeFontText());
        add(this.original);
        this.original.setEnabled(true);
        this.original.setSelected(true);
        this.original.addActionListener(this);

        this.probability = new JRadioButton();
        this.probability.setName("Probability");
        this.probability.setText("Probability");
        this.probability.setFont(DefaultSettings.setSegoeFontText());
        add(this.probability);
        this.probability.addActionListener(this);
    }

    @Override
    protected void createButtonGroup() {
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(original);
        buttonGroup.add(probability);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == this.original) {
            probabilityListener.setIsDataViewProbability(false);
            probabilityListener.initiateEnsembleCompute(ChartType.SCATTERPLOT);
        } else if(e.getSource() == this.probability) {
            probabilityListener.setIsDataViewProbability(true);
            probabilityListener.initiateEnsembleCompute(ChartType.SCATTERPLOT);
        }
    }

    private void resetRadioButton() {
        original.setSelected(true);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getSource() instanceof DatabaseHandlerService) {
            resetRadioButton();
            initiateButtonSelection();
        }
    }
}
