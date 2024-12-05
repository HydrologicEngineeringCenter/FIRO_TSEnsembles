package hec.ensembleview.viewpanels;

import hec.ensembleview.DatabaseHandlerService;
import hec.ensembleview.DefaultSettings;
import hec.ensembleview.charts.ChartType;
import hec.ensembleview.controllers.DataViewListener;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;

public abstract class DataTransformView extends JPanel implements ActionListener, PropertyChangeListener {
    protected DataTransformView() {
        DatabaseHandlerService.getInstance().addDatabaseChangeListener(this);

        setupDataViewPanel();
        initiateRadioButton();
        createButtonGroup();
    }

    private void setupDataViewPanel() {
        GridBagLayout layout = new GridBagLayout();
        setLayout(layout);

        Border grayLine = BorderFactory.createLineBorder(Color.LIGHT_GRAY);
        setBorder((BorderFactory.createTitledBorder(grayLine, "Data View", TitledBorder.LEFT, TitledBorder.TOP)));
        ((TitledBorder) getBorder()).setTitleFont(DefaultSettings.setSegoeFontTitle());
    }

    public abstract void setListener(DataViewListener listener);

    protected abstract ChartType getChartType();

    protected abstract void initiateButtonSelection();

    protected abstract void initiateRadioButton();

    protected abstract void createButtonGroup();
}
