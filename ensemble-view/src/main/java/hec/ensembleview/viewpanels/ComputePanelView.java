package hec.ensembleview.viewpanels;

import hec.ensembleview.DatabaseHandlerService;
import hec.ensembleview.DefaultSettings;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

public abstract class ComputePanelView extends JPanel implements ItemListener, ActionListener, PropertyChangeListener {
    private final List<JCheckBox> statisticsList = new ArrayList<>();
    private final List<JTextField> textFieldList = new ArrayList<>();

    protected ComputePanelView() {
        DatabaseHandlerService.getInstance().addDatabaseChangeListener(this);
        initCheckBoxStat();
        initTextBoxStat();
        addCheckboxListeners(statisticsList);
        addTextBoxListeners(textFieldList);
        setupStatsPanel();
        setDefaultFont(statisticsList);
        setVisible(true);
    }

    void setupStatsPanel() {
        setLayout(new GridLayout(3, 1));
        Border graylineBorder = BorderFactory.createLineBorder(Color.LIGHT_GRAY);
        setBorder(BorderFactory.createTitledBorder(graylineBorder, "Statistics", TitledBorder.LEFT, TitledBorder.TOP));
        ((TitledBorder) getBorder()).setTitleFont(DefaultSettings.setSegoeFontTitle());
    }

    void setDefaultFont(List<JCheckBox> list) {
        for(JCheckBox checkBox : list) {
            checkBox.setFont(DefaultSettings.setSegoeFontText());
        }
    }

    protected void setTextFieldAction(JTextField textField, boolean isEnable) {
        textField.setEditable(isEnable);
        textField.setEnabled(isEnable);
    }

    void addCheckboxListeners(List<JCheckBox> list) {  // add checkbox Listeners
        for(JCheckBox checkBox : list) {
            checkBox.addItemListener(this);
        }
    }

    protected void addCheckBoxList(JCheckBox checkBox) {  // list of existing checkboxes in panel
        statisticsList.add(checkBox);
    }

    protected void addTextFieldList(JTextField textField) {
        textFieldList.add(textField);
    }

    void addTextBoxListeners(List<JTextField> list) {
        for(JTextField textField : list) {
            textField.addActionListener(this);
        }
    }

    protected JTextField createTextBox(String text) {
        JTextField textField = new JTextField();
        textField.setName(text);
        return textField;
    }

    protected JCheckBox createCheckBox(String text) {  // create new checkbox
        JCheckBox checkBox = new JCheckBox();
        checkBox.setText(text);
        checkBox.setName(text);
        return checkBox;
    }

    public void resetCheckboxes() {
        for(JCheckBox checkBox : statisticsList) {
            checkBox.setSelected(false);
        }
    }

    protected void groupCheckboxTextfield(JCheckBox checkBox, JTextField textField) {
        JPanel group = new JPanel();
        group.setLayout(new GridLayout());
        group.add(checkBox);
        group.add(textField);
        add(group);
    }

    protected abstract void initCheckBoxStat();

    protected abstract void initTextBoxStat();

}
