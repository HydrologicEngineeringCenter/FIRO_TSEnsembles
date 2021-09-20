package hec.ensemble;

import org.junit.jupiter.api.Test;

public class TestPerformance {
    @Test
    public void testPerformance() {
        String[] args = {"c:/temp/hefs_cache", "100","10","C:/temp/ResSim.db"};
        try {
            EnsembleUtility.main(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
