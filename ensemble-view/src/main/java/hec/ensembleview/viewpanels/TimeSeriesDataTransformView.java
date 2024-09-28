package hec.ensembleview.viewpanels;

import hec.ensembleview.DatabaseHandlerService;
import hec.ensembleview.DefaultSettings;
import hec.ensembleview.charts.ChartType;
import hec.ensembleview.controllers.DataViewListener;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;

public class TimeSeriesDataTransformView extends DataTransformView {
    private JRadioButton original;
    private JRadioButton cumulative;
    private transient DataViewListener cumulativeListener;
    private static final ChartType CHART_TYPE = ChartType.TIMEPLOT;
    public TimeSeriesDataTransformView() {
        super();
    }

    @Override
    public void setListener(DataViewListener listener) {
        this.cumulativeListener = listener;
    }

    @Override
    protected ChartType getChartType() {
        return CHART_TYPE;
    }

    @Override
    protected void initiateButtonSelection() {
        if(original.isSelected()) {
            actionPerformed(new ActionEvent(original, ActionEvent.ACTION_PERFORMED, "selected"));
        } else if (cumulative.isSelected()) {
            actionPerformed(new ActionEvent(cumulative, ActionEvent.ACTION_PERFORMED, "selected"));
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

        this.cumulative = new JRadioButton();
        this.cumulative.setName("Cumulative");
        this.cumulative.setText("Cumulative");
        this.cumulative.setFont(DefaultSettings.setSegoeFontText());
        add(this.cumulative);
        this.cumulative.addActionListener(this);
    }

    @Override
    protected void createButtonGroup() {
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(original);
        buttonGroup.add(cumulative);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == this.original) {
            cumulativeListener.setIsDataViewCumulative(false);
            cumulativeListener.initiateCompute(ChartType.TIMEPLOT);
        } else if(e.getSource() == this.cumulative) {
            cumulativeListener.setIsDataViewCumulative(true);
            cumulativeListener.initiateCompute(ChartType.TIMEPLOT);
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
