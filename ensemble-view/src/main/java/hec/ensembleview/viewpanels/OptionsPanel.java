package hec.ensembleview.viewpanels;

import hec.RecordIdentifier;
import hec.ensembleview.controllers.DatabaseListener;
import hec.ensembleview.DefaultSettings;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.time.ZonedDateTime;

public class OptionsPanel extends JPanel {
    private JPanel parentPanel;
    private JPanel filePathPanel;
    private JTextField filePath;
    private JComboBox<RecordIdentifier> locations;
    private JComboBox<ZonedDateTime> dateTimes;
    private transient ActionListener filePathListener;
    private transient ActionListener locationsListener;
    private transient ActionListener dateTimesListener;
    private transient DatabaseListener databaseListener;

    public OptionsPanel() {
        setLayout(new GridLayout(1, 1));

        addFilePathListener();
        addLocationsListener();
        addDateTimeListener();

        parentPanel = createParentPanel();
    }

    public void setDatabaseListener(DatabaseListener listener) {
        this.databaseListener = listener;
    }

    private void addFilePathListener() {

        filePathListener = e -> {
        /*
    Add listeners to file path button, locations combo box, date/time combo box, and statistics combo boxes.
     */

            databaseListener.setIsNewLoad(false);
            databaseListener.initialize(filePathPanel, dateTimes, locations, filePath);
        };
    }

    private void addLocationsListener() {
        locationsListener = e -> {
            if(databaseListener.getIsNewLoad()) {
                databaseListener.setSelectedRid();
            }
        };
    }

    private void addDateTimeListener() {
        dateTimesListener = e -> {
            if(databaseListener.getIsNewLoad()) {
                databaseListener.setZdt();
            }
        };
    }

    private JPanel createParentPanel() {
                /*
        Create panel that holds file name, location, and date/time information.
         */
        parentPanel = new JPanel();
        Border graylineBorder = BorderFactory.createLineBorder(Color.LIGHT_GRAY);
        parentPanel.setBorder(BorderFactory.createTitledBorder(graylineBorder, "Options", TitledBorder.LEFT, TitledBorder.TOP));
        ((TitledBorder) parentPanel.getBorder()).setTitleFont(DefaultSettings.setSegoeFontTitle());
        GridLayout experimentLayout = new GridLayout(3,2);
        parentPanel.setLayout(experimentLayout);

        add(parentPanel);
        filePathPanel = createFilePathPanel();
        locations = createLocationsComboBox();
        dateTimes = createDateTimeComboBox();

        return parentPanel;
    }

    private JPanel createFilePathPanel() {
                /*
        Create file select area.
         */
        JLabel text = new JLabel("File");
        text.setFont(DefaultSettings.setSegoeFontText());
        parentPanel.add(text);

        filePathPanel = new JPanel();
        GridLayout layout = new GridLayout(1,2);
        filePathPanel.setLayout(layout);

        filePath = new JTextField();
        filePath.setEditable(false);
        filePathPanel.add(filePath);

        JButton fileSearchButton = new JButton();
        fileSearchButton.addActionListener(filePathListener);
        fileSearchButton.setText("Choose File...");
        fileSearchButton.setFont(DefaultSettings.setSegoeFontText());
        filePathPanel.add(fileSearchButton);
        parentPanel.add(filePathPanel);

        return filePathPanel;
    }

    private JComboBox<RecordIdentifier> createLocationsComboBox() {
                /*
        Create location combo box.
         */
        JLabel location = new JLabel("Location");
        location.setFont(DefaultSettings.setSegoeFontText());

        parentPanel.add(location);
        locations = new JComboBox<>();
        locations.setFont(DefaultSettings.setSegoeFontText());

        parentPanel.add(locations);
        locations.addActionListener(locationsListener);

        return locations;
    }

    private JComboBox<ZonedDateTime> createDateTimeComboBox() {
                /*
        Create location combo box.
         */
        JLabel dateTime = new JLabel("Date/Time");
        dateTime.setFont(DefaultSettings.setSegoeFontText());

        parentPanel.add(dateTime);
        dateTimes = new JComboBox<>();
        parentPanel.add(dateTimes);
        dateTimes.addActionListener(dateTimesListener);

        return dateTimes;
    }
}
