package hec.ensemble.stats;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NearestIndexComputableTest {

    private final float[] values = {50, 10, 80, 30, 70, 20, 60, 40};

    @Test
    void testNearestToMax() {
        ComputableIndex computable = new NearestIndexComputable(new MaxComputable());
        int index = computable.compute(values);
        assertEquals(2, index); // values[2] = 80 is the max
    }

    @Test
    void testNearestToMin() {
        ComputableIndex computable = new NearestIndexComputable(new MinComputable());
        int index = computable.compute(values);
        assertEquals(1, index); // values[1] = 10 is the min
    }

    @Test
    void testNearestToMean() {
        // mean = (50+10+80+30+70+20+60+40)/8 = 45
        // values[0]=50 (diff=5) is found first in linear scan among ties
        ComputableIndex computable = new NearestIndexComputable(new MeanComputable());
        int index = computable.compute(values);
        assertEquals(0, index);
    }

    @Test
    void testNearestToMedian() {
        ComputableIndex computable = new NearestIndexComputable(new MedianComputable());
        int index = computable.compute(values);
        // median of sorted {10,20,30,40,50,60,70,80} = (40+50)/2 = 45
        // values[0]=50 (diff=5) is found first
        assertEquals(0, index);
    }

    @Test
    void testNearestToPercentileHigh() {
        ComputableIndex nearest = new NearestIndexComputable(new PercentilesComputable(0.95f));
        float[] testValues = {1, 2, 3, 4, 5, 6, 7, 8};
        int index = nearest.compute(testValues);
        assertEquals(7, index); // values[7]=8, closest to 95th percentile (7.65)
    }

    @Test
    void testNearestToPercentileLow() {
        ComputableIndex nearest = new NearestIndexComputable(new PercentilesComputable(0.05f));
        float[] testValues = {1, 2, 3, 4, 5, 6, 7, 8};
        int index = nearest.compute(testValues);
        assertEquals(0, index); // values[0]=1, closest to 5th percentile (1.35)
    }

    @Test
    void testNearestToPercentileUnsortedInput() {
        ComputableIndex nearest = new NearestIndexComputable(new PercentilesComputable(0.5f));
        float[] testValues = {50, 10, 80, 30, 70, 20, 60, 40};
        int index = nearest.compute(testValues);
        // 50th percentile of sorted {10,20,30,40,50,60,70,80} = 45
        // values[0]=50 (diff=5) is found first in linear scan
        assertEquals(0, index);
    }
}