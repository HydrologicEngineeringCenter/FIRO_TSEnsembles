package hec.ensembleview;

import java.awt.*;

public class DefaultSettings {
    private DefaultSettings() {
    }

    public static Font setSegoeFontText() {
        return new Font("Segoe UI", Font.PLAIN, 12);
    }

    public static Font setSegoeFontTitle()  {
        return new Font("Segoe UI", Font.BOLD, 14);
    }
}
