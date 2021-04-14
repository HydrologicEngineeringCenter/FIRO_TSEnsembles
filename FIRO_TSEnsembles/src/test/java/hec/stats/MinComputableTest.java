package hec.stats;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MinComputableTest {
    @Test
    public void testMinCompute() {
        MinComputable test = new MinComputable();
        float[] num = {1,2,3,4,5,6,7,8};
        float results = test.compute(num);
        assertEquals(1, results);
    }
    @Test
    public void testMeanCompute2() {
        MinComputable test = new MinComputable();
        float[] num = {10,30,45,80,50};
        float results = test.compute(num);
        assertEquals(10, results);
    }
}
