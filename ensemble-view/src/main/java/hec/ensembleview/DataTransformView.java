package hec.ensembleview;

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

    protected void setupDataViewPanel() {
        GridBagLayout layout = new GridBagLayout();
        setLayout(layout);

        Border grayLine = BorderFactory.createLineBorder(Color.LIGHT_GRAY);
        setBorder((BorderFactory.createTitledBorder(grayLine, "Data View", TitledBorder.LEFT, TitledBorder.TOP)));
        ((TitledBorder) getBorder()).setTitleFont(DefaultSettings.setSegoeFontTitle());
    }

    protected abstract void initiateButtonSelection();

    protected abstract void initiateRadioButton();

    protected abstract void createButtonGroup();
}
