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

public class TimeSeriesDataTransformView extends JPanel implements ActionListener, PropertyChangeListener {
    private JRadioButton original;
    private JRadioButton cumulative;
    private final transient ComputePanelController computePanelController;
    public TimeSeriesDataTransformView(ComputePanelController computePanelController) {
        this.computePanelController = computePanelController;
        DatabaseHandlerService.getInstance().addDatabaseChangeListener(this);

        setupDataViewPanel();
        initiateRadioButton();
        createButtonGroup();
    }

    private void initiateButtonSelection() {
        if(original.isSelected()) {
            actionPerformed(new ActionEvent(original, ActionEvent.ACTION_PERFORMED, "selected"));
        } else if (cumulative.isSelected()) {
            actionPerformed(new ActionEvent(cumulative, ActionEvent.ACTION_PERFORMED, "selected"));
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

        this.cumulative = new JRadioButton();
        this.cumulative.setName("Cumulative");
        this.cumulative.setText("Cumulative");
        this.cumulative.setFont(DefaultSettings.setSegoeFontText());
        add(this.cumulative);
        this.cumulative.addActionListener(this);
    }

    private void createButtonGroup() {
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(original);
        buttonGroup.add(cumulative);
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
            computePanelController.setIsDataViewCumulative(false);
            computePanelController.initiateTimeSeriesCompute(ChartType.TIMEPLOT);
        } else if(e.getSource() == this.cumulative) {
            computePanelController.setIsDataViewCumulative(true);
            computePanelController.initiateTimeSeriesCompute(ChartType.TIMEPLOT);
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
