package hec.ensembleview.viewpanels;

import hec.RecordIdentifier;
import hec.ensembleview.DefaultSettings;
import hec.ensembleview.controllers.DatabaseListener;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.time.ZonedDateTime;
import java.util.Objects;

public class OptionsPanel extends JPanel {
    private JLabel fileText;
    private JLabel locationText;
    private JLabel dateTimeText;
    private JPanel parentPanel;
    private JPanel filePathPanel;
    private JTextField filePath;
    private JButton fileSearchButton;
    private JComboBox<RecordIdentifier> locations;
    private JComboBox<ZonedDateTime> dateTimes;
    private transient ActionListener filePathListener;
    private transient ActionListener locationsListener;
    private transient ActionListener dateTimesListener;
    private transient DatabaseListener databaseListener;

    public OptionsPanel() {
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
        setLayout(new GridLayout());

        parentPanel = new JPanel();
        parentPanel.setLayout(new GridBagLayout());
        Border graylineBorder = BorderFactory.createLineBorder(Color.LIGHT_GRAY);
        parentPanel.setBorder(BorderFactory.createTitledBorder(graylineBorder, "Options", TitledBorder.LEFT, TitledBorder.TOP));
        ((TitledBorder) parentPanel.getBorder()).setTitleFont(DefaultSettings.setSegoeFontTitle());

        add(parentPanel);
        filePathPanel = createFilePathPanel();
        locations = createLocationsComboBox();
        dateTimes = createDateTimeComboBox();

        setUpOptionsPanelLayout();

        return parentPanel;
    }

    private void setUpOptionsPanelLayout() {
        // Set up text field, text box, and combo box locations
        GridBagConstraints gc = new GridBagConstraints();
        Dimension dim = new Dimension();  // dimensions for text field
        dim.width = 250;
        dim.height = 25;

        // File Text Field and button selection
        gc.gridx = 0;
        gc.gridy = 0;
        gc.weightx = .1;
        gc.weighty = .5;

        gc.anchor = GridBagConstraints.LINE_START;
        parentPanel.add(fileText, gc);

        gc.gridx = 1;
        gc.gridy = 0;
        gc.weightx = .9;
        gc.weighty = .5;

        Dimension buttonDim = new Dimension();
        buttonDim.width = 25;
        buttonDim.height = 25;
        fileSearchButton.setMaximumSize(buttonDim);
        fileSearchButton.setMinimumSize(buttonDim);
        fileSearchButton.setPreferredSize(buttonDim);

        filePath.setPreferredSize(dim);
        gc.anchor = GridBagConstraints.LINE_START;
        parentPanel.add(filePathPanel, gc);

        // Location Combo Box Selection
        gc.gridx = 0;
        gc.gridy = 1;
        gc.weightx = .1;
        gc.weighty = .5;

        gc.anchor = GridBagConstraints.LINE_START;
        parentPanel.add(locationText, gc);

        Dimension comboDim = new Dimension();
        comboDim.width = 275;
        comboDim.height = 25;

        gc.gridx = 1;
        gc.gridy = 1;
        gc.weightx = .9;
        gc.weighty = .5;
        gc.insets = new Insets(5, 0, 5, 0);

        locations.setPreferredSize(comboDim);
        gc.anchor = GridBagConstraints.LINE_START;
        parentPanel.add(locations, gc);

        // Date and Time Combo Box Selection
        gc.gridx = 0;
        gc.gridy = 2;
        gc.weightx = .1;
        gc.weighty = .5;

        gc.anchor = GridBagConstraints.LINE_START;
        parentPanel.add(dateTimeText, gc);

        gc.gridx = 1;
        gc.gridy = 2;
        gc.weightx = .9;
        gc.weighty = .5;
        gc.insets = new Insets(0, 0, 5, 0);

        dateTimes.setPreferredSize(comboDim);
        gc.anchor = GridBagConstraints.LINE_START;
        parentPanel.add(dateTimes, gc);
    }

    private JPanel createFilePathPanel() {
                /*
        Create file select area.
         */
        fileText = new JLabel("File");
        fileText.setFont(DefaultSettings.setSegoeFontText());

        fileSearchButton = new JButton();
        fileSearchButton.addActionListener(filePathListener);
        ImageIcon fileIcon = new ImageIcon(Objects.requireNonNull(this.getClass().getResource("/Open24_Default.gif")));

        fileSearchButton.setIcon(fileIcon);
        filePath = new JTextField();
        filePath.setEditable(false);

        filePathPanel = new JPanel();
        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints gc = new GridBagConstraints();
        filePathPanel.setLayout(layout);

        gc.gridx = 0;
        gc.gridy = 0;
        gc.weightx = .9;

        filePathPanel.add(filePath, gc);

        gc.gridx = 1;
        gc.gridy = 0;
        gc.weightx = .1;
        filePathPanel.add(fileSearchButton, gc);

        return filePathPanel;
    }

    private JComboBox<RecordIdentifier> createLocationsComboBox() {
        locationText = new JLabel("Location");
        locationText.setFont(DefaultSettings.setSegoeFontText());

        locations = new JComboBox<>();
        locations.setFont(DefaultSettings.setSegoeFontText());

        locations.addActionListener(locationsListener);

        return locations;
    }

    private JComboBox<ZonedDateTime> createDateTimeComboBox() {
        dateTimeText = new JLabel("Date/Time");
        dateTimeText.setFont(DefaultSettings.setSegoeFontText());

        dateTimes = new JComboBox<>();
        dateTimes.addActionListener(dateTimesListener);

        return dateTimes;
    }
}
