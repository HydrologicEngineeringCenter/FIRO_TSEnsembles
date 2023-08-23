package hec.ensembleview.viewpanels;

import hec.ensembleview.DatabaseHandlerService;
import hec.ensembleview.DefaultSettings;
import hec.ensembleview.controllers.SingleValueDataViewListener;
import hec.ensembleview.mappings.SingleValueSummaryType;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;

public class SingleValueDataTransformView extends DataTransformView {
    private JRadioButton acrossTime;
    private JRadioButton acrossEnsembles;
    private transient SingleValueDataViewListener singleValueDataViewListener;
    public SingleValueDataTransformView() {
        super();
        Border grayLine = BorderFactory.createLineBorder(Color.LIGHT_GRAY);
        setBorder((BorderFactory.createTitledBorder(grayLine, "Compute Order", TitledBorder.LEFT, TitledBorder.TOP)));
        ((TitledBorder) getBorder()).setTitleFont(DefaultSettings.setSegoeFontTitle());
    }

    public void setSingleValueDataViewListener(SingleValueDataViewListener listener) {
        this.singleValueDataViewListener = listener;
    }

    @Override
    protected void initiateButtonSelection() {
        if(acrossTime.isSelected()) {
            actionPerformed(new ActionEvent(acrossTime, ActionEvent.ACTION_PERFORMED, "selected"));
        } else if (acrossEnsembles.isSelected()) {
            actionPerformed(new ActionEvent(acrossEnsembles, ActionEvent.ACTION_PERFORMED, "selected"));
        }
    }

    @Override
    protected void initiateRadioButton() {
        this.acrossEnsembles = new JRadioButton();
        this.acrossEnsembles.setName("Across Ensembles");
        this.acrossEnsembles.setText("Across Ensembles");
        this.acrossEnsembles.setFont(DefaultSettings.setSegoeFontText());
        add(this.acrossEnsembles);
        this.acrossEnsembles.setEnabled(true);
        this.acrossEnsembles.setSelected(true);
        this.acrossEnsembles.addActionListener(this);

        this.acrossTime = new JRadioButton();
        this.acrossTime.setName("Across Time");
        this.acrossTime.setText("Across Time");
        this.acrossTime.setFont(DefaultSettings.setSegoeFontText());
        add(this.acrossTime);
        this.acrossTime.addActionListener(this);
    }

    @Override
    protected void createButtonGroup() {
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(acrossEnsembles);
        buttonGroup.add(acrossTime);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == this.acrossTime) {
            singleValueDataViewListener.initiateComboBoxSelection(SingleValueSummaryType.COMPUTEACROSSTIME);
            singleValueDataViewListener.setComputeOrderLabel("Across Time");
        } else if(e.getSource() == this.acrossEnsembles) {
            singleValueDataViewListener.initiateComboBoxSelection(SingleValueSummaryType.COMPUTEACROSSENSEMBLES);
            singleValueDataViewListener.setComputeOrderLabel("Across Ensembles");
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getSource() instanceof DatabaseHandlerService) {
            initiateButtonSelection();
        }
    }
}
