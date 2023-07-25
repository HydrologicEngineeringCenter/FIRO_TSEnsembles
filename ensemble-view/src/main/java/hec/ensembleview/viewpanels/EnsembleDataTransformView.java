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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class EnsembleDataTransformView extends JPanel implements ActionListener, PropertyChangeListener {
    private JRadioButton original;
    private JRadioButton probability;
    private final transient ComputePanelController computePanelController;
    public EnsembleDataTransformView(ComputePanelController computePanelController) {
        this.computePanelController = computePanelController;
        DatabaseHandlerService.getInstance().addDatabaseChangeListener(this);

        setupDataViewPanel();
        initiateRadioButton();
        createButtonGroup();
    }

    private void initiateButtonSelection() {
        if(original.isSelected()) {
            actionPerformed(new ActionEvent(original, ActionEvent.ACTION_PERFORMED, "selected"));
        } else if (probability.isSelected()) {
            actionPerformed(new ActionEvent(probability, ActionEvent.ACTION_PERFORMED, "selected"));
        }
    }

    private void initiateRadioButton() {
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

    private void createButtonGroup() {
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(original);
        buttonGroup.add(probability);
    }

    private void setupDataViewPanel() {
        GridBagLayout layout = new GridBagLayout();
        setLayout(layout);

        Border grayLine = BorderFactory.createLineBorder(Color.LIGHT_GRAY);
        setBorder((BorderFactory.createTitledBorder(grayLine, "Data View", TitledBorder.LEFT, TitledBorder.TOP)));
        ((TitledBorder) getBorder()).setTitleFont(DefaultSettings.setSegoeFontTitle());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == this.original) {
            computePanelController.setIsDataViewProbability(false);
            computePanelController.initiateEnsembleCompute(ChartType.SCATTERPLOT);
        } else if(e.getSource() == this.probability) {
            computePanelController.setIsDataViewProbability(true);
            computePanelController.initiateEnsembleCompute(ChartType.SCATTERPLOT);
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
