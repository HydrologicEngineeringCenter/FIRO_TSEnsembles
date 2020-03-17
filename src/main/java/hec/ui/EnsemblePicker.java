 package hec.ui;

 import javax.swing.*;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.table.TableModel;
 import java.awt.*;

 public class EnsemblePicker extends JDialog {
     final JTable table;
     private int selectedRow=-1;
     public EnsemblePicker(Frame parent, TableModel model) {
         super(parent,true);


         setLayout(new GridLayout(2, 1));
         table = new JTable(model);

         table.setDefaultEditor(Object.class, null);//  read-only https://stackoverflow.com/questions/1990817/how-to-make-a-jtable-non-editable
         table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         table.setPreferredScrollableViewportSize(new Dimension(500, 70));
         table.setFillsViewportHeight(true);
         add(new JScrollPane(table));
         table.getSelectionModel().addListSelectionListener(event -> tableChanged(event));

         JButton button = new JButton();
         button.setText("OK");
         add(button);
         button.addActionListener(event -> dispose());
          pack();
         setTitle("Select Ensemble");
         setLocationRelativeTo(getParent());

     }
     public void tableChanged(ListSelectionEvent e) {
         selectedRow = table.getSelectedRow();
     }
     public int getSelectedRow() {
         return selectedRow;
     }
 }
