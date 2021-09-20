package hec.ensemble.ui;

import hec.SqliteDatabase;
import hec.RecordIdentifier;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class EnsemblePickerDemo  extends  JFrame implements ActionListener {

    private JButton button;

    public EnsemblePickerDemo()
    {
        init();
    }

    private void init() {
        setTitle("Demo program that uses Ensemble Picker");
        setSize(300,300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        button = new JButton("Select Ensemble...");
        button.addActionListener(this);
        add(button);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        showEnsemblePicker();

    }

    private void showEnsemblePicker()
    {
        try {
            String fileName = "C:\\temp\\ResSim.db";
            SqliteDatabase db = new SqliteDatabase(fileName, SqliteDatabase.CREATION_MODE.OPEN_EXISTING_UPDATE);
            List<RecordIdentifier> locations = db.getEnsembleTimeSeriesIDs();
            TableModel model = getTableModel(locations);

            EnsemblePicker picker = new EnsemblePicker(this,model);
            picker.setVisible(true);
             int idx = picker.getSelectedRow();
             button.setText("selectedIndex = "+idx);
            System.out.println(idx);


        }catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }


    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {

            EnsemblePickerDemo d = new EnsemblePickerDemo();
            d.setVisible(true);
        });
    }

    private static TableModel getTableModel(List<RecordIdentifier> locations) {
        String[] columnNames = {"Location", "Parameter"};
        List<String[]> values = new ArrayList<String[]>();

        for (RecordIdentifier loc : locations) {
            values.add(new String[]{loc.location, loc.parameter});
        }
        return new DefaultTableModel(values.toArray(new Object[][]{}), columnNames);
    }

}

