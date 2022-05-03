package hec.stats;

import hec.ensemble.Ensemble;
import hec.ensemble.EnsembleConfiguration;
import hec.ensemble.Logger;
import hec.ensemble.TestData;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class ComputableComputableTest {
    @Test
    public void testComputableComputableAcrossTimeSimpleArray() {
        SingleComputable test = new ComputableComputable(new MaxComputable(), new MeanComputable(), true);
        float[] num1 = {11,2,6,4,5,6,7,8};
        float[] num2 = {1,2,3,2,5,6,9,9};
        float[][] vals = {num1,num2};
        float results = test.compute(vals);
        assertEquals(10, results);
    }

    @Test
    public void testComputableComputableWithDurationAcrossTimeSimpleArray() {
        Computable computeDuration = new MaxAccumDuration(2);
        SingleComputable test = new ComputableComputable(computeDuration, new MeanComputable(), true);
        Configurable c = (Configurable) computeDuration;
        c.configure(new EnsembleConfiguration(null, null, Duration.ofHours(1),""));
        float[] num1 = {11,2,6,4,5,6,7,8,20,14};
        float[] num2 = {1,2,3,2,5,6,9,9,4,35};
        float[][] vals = {num1,num2};
        float results = test.compute(vals);
        assertEquals(36.5, results);
    }

    @Test
    public void testComputableComputableAcrossTracesSimpleArray() {
        SingleComputable test = new ComputableComputable(new MaxComputable(), new MeanComputable(), false);
        float[] num1 = {11,2,6,4,5,6,7,8};
        float[] num2 = {1,2,3,2,5,6,9,9};
        float[][] vals = {num1,num2};
        float results = test.compute(vals);
        assertEquals(6.5, results);
    }


    @Test
    public void testComputableComputableWithDurationAcrossTimeEnsemble() {
        try {
            Ensemble e = TestData.getSampleEnsemble();
            SingleComputable test = new ComputableComputable(new MaxAccumDuration(48), new MeanComputable(), true);
            float output = e.singleComputeForEnsemble(test);
            assertEquals(210.24407958984375, output);
        } catch (Exception e) {
            Logger.logError(e);
            fail();
        }
    }

    @Test
    public void testComputableComputableAcrossTimeEnsemble() {
        try {
            Ensemble e = TestData.getSampleEnsemble();
            SingleComputable test = new ComputableComputable(new MaxComputable(), new MeanComputable(), true);

            float output = e.singleComputeForEnsemble(test);
            assertEquals(12.504984855651855, output);
        } catch (Exception e) {
            Logger.logError(e);
            fail();
        }
    }

    @Test
    public void testComputableComputableAcrossTracesEnsemble() {
        try {
            Ensemble e = TestData.getSampleEnsemble();
            SingleComputable test = new ComputableComputable(new MaxComputable(), new MeanComputable(), false);

            float output = e.singleComputeForEnsemble(test);
            assertEquals(8.83727741241455, output);
        } catch (Exception e) {
            Logger.logError(e);
            fail();
        }
    }

}
