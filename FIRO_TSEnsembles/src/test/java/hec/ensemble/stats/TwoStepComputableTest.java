package hec.ensemble.stats;

import hec.ensemble.Ensemble;
import hec.ensemble.EnsembleConfiguration;
import hec.ensemble.Logger;
import hec.ensemble.TestData;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class TwoStepComputableTest {
    @Test
    public void testComputableComputableAcrossTimeSimpleArray() {
        SingleValueComputable test = new TwoStepComputableSingleMetricValue(new MaxComputable(), new MeanComputable(), true);
        float[] num1 = {11,2,6,4,5,6,7,8};
        float[] num2 = {1,2,3,2,5,6,9,9};
        float[][] vals = {num1,num2};
        float results = test.compute(vals);
        assertEquals(6.5, results);
    }

    @Test
    public void testComputableComputableWithDurationAcrossTimeSimpleArray() {
        Computable computeDuration = new MaxAccumDuration(2);
        SingleValueComputable test = new TwoStepComputableSingleMetricValue(computeDuration, new MeanComputable(), true);
        Configurable c = (Configurable) computeDuration;
        c.configure(new EnsembleConfiguration(null, null, Duration.ofHours(1),""));
        float[] num1 = {11,2,6,4,5,6,7,8,20,14};
        float[] num2 = {1,2,3,2,5,6,9,9,4,35};
        float[][] vals = {num1,num2};
        float results = test.compute(vals);
        assertEquals(1.3140, results, 0.001);
    }

    @Test
    public void testComputableComputableWithPercentilesAcrossTracesSimpleArray() {
        Computable computeDuration = new MaxAccumDuration(2);
        SingleValueComputable test = new TwoStepComputableSingleMetricValue(computeDuration, new PercentilesComputable(0.9f), false);
        Configurable c = (Configurable) computeDuration;
        c.configure(new EnsembleConfiguration(null, null, Duration.ofHours(1),""));
        float[] num1 = {11,2,6,4,5,6,7,8,20,14};
        float[] num2 = {1,2,3,2,5,6,9,9,4,35};
        float[][] vals = {num1,num2};
        float results = test.compute(vals);
        assertEquals(3.1818, results, 0.001);
    }

    @Test
    public void testComputableComputableAcrossTracesSimpleArray() {
        SingleValueComputable test = new TwoStepComputableSingleMetricValue(new MaxComputable(), new MeanComputable(), false);
        float[] num1 = {11,2,6,4,5,6,7,8};
        float[] num2 = {1,2,3,2,5,6,9,9};
        float[][] vals = {num1,num2};
        float results = test.compute(vals);
        assertEquals(10, results);
    }


    @Test
    public void testComputableComputableWithDurationAcrossTimeEnsemble() {
        try {
            Ensemble e = TestData.getSampleEnsemble();
            SingleValueComputable test = new TwoStepComputableSingleMetricValue(new MaxAccumDuration(48), new MeanComputable(), true);
            float output = e.singleComputeForEnsemble(test);
            assertEquals(-281.292, output, 0.001);
        } catch (Exception e) {
            Logger.logError(e);
            fail();
        }
    }

    @Test
    public void testComputableComputableWithPercentilesAcrossTimeEnsemble() {
        try {
            Ensemble e = TestData.getSampleEnsemble();
            SingleValueComputable test = new TwoStepComputableSingleMetricValue(new MaxAccumDuration(2), new PercentilesComputable(0.9f), true);
            float output = e.singleComputeForEnsemble(test);
            assertEquals(2.6652, output, 0.001);
        } catch (Exception e) {
            Logger.logError(e);
            fail();
        }
    }

    @Test
    public void testComputableComputableWithPercentilesAcrossTracesEnsemble() {
        try {
            Ensemble e = TestData.getSampleEnsemble();
            SingleValueComputable test = new TwoStepComputableSingleMetricValue(new MaxAccumDuration(2), new PercentilesComputable(0.9f), false);
            float output = e.singleComputeForEnsemble(test);
            assertEquals(5.234, output, 0.001);
        } catch (Exception e) {
            Logger.logError(e);
            fail();
        }
    }

    @Test
    public void testComputableComputableAcrossTimeEnsemble() {
        try {
            Ensemble e = TestData.getSampleEnsemble();
            SingleValueComputable test = new TwoStepComputableSingleMetricValue(new MaxComputable(), new MeanComputable(), true);

            float output = e.singleComputeForEnsemble(test);
            assertEquals(8.83727741241455, output);
        } catch (Exception e) {
            Logger.logError(e);
            fail();
        }
    }

    @Test
    public void testComputableComputableAcrossTracesEnsemble() {
        try {
            Ensemble e = TestData.getSampleEnsemble();
            SingleValueComputable test = new TwoStepComputableSingleMetricValue(new MaxComputable(), new MeanComputable(), false);

            float output = e.singleComputeForEnsemble(test);
            assertEquals(12.504984855651855, output);
        } catch (Exception e) {
            Logger.logError(e);
            fail();
        }
    }

}
