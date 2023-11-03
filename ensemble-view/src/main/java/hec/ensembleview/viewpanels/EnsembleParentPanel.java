package hec.ensembleview.viewpanels;

import javax.swing.*;
import java.awt.*;

public class EnsembleParentPanel extends JFrame{

    public EnsembleParentPanel(JPanel topPanel, JTabbedPane tabPane) {
        setTitle("Ensemble Viewer");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(true);
        add(topPanel, BorderLayout.NORTH);
        add(tabPane, BorderLayout.CENTER);
        setSize(1000,1000);

        setVisible(true);
    }
}
