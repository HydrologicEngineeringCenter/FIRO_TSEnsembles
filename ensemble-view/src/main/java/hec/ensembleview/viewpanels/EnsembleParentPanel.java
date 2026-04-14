package hec.ensembleview.viewpanels;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class EnsembleParentPanel extends JFrame {

    public EnsembleParentPanel() {
        setTitle("Ensemble Viewer");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(true);
        setSize(1000, 1000);
        ImageIcon fileIcon = new ImageIcon(Objects.requireNonNull(this.getClass().getResource("/EnsembleAnalysis24_Default.gif")));
        setIconImage(fileIcon.getImage());
    }

    public void setContents(JPanel topPanel, Component centerContent) {
        add(topPanel, BorderLayout.NORTH);
        add(centerContent, BorderLayout.CENTER);
        setVisible(true);
    }
}
