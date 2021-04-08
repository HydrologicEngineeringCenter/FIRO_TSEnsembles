package hec.stats;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MeanComputableTest {
    @Test
    public void testMeanCompute() {
        MeanComputable test = new MeanComputable();
        float[] num = {1,2,3,4,5,6,7,8};
        float results = test.compute(num);
        assertEquals(4.5, results);
    }

    {
        MeanComputable test = new MeanComputable();
        float[] num = {10,30,45,80,50};
        float results = test.compute(num);
        assertEquals(43, results);
    }
}
