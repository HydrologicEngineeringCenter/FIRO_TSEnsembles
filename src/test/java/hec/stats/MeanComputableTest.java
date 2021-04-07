package hec.stats;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MeanComputableTest {
    @Test
    public void testMeanCompute() {
        MeanComputable test = new MeanComputable();
        float[] num = {1,2,3,4,5,6};
        float results = test.compute(num);
        assertEquals(3.5, results);
    }
}
