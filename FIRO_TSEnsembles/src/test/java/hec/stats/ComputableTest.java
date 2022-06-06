package hec.stats;

import hec.ensemble.Ensemble;
import hec.ensemble.TestData;
import org.jdom.Element;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ComputableTest {
    Ensemble e = TestData.getSampleEnsemble();
    Computable maxTest = new MaxComputable();
    Computable maxAccumDuration = new MaxAccumDuration(3);
    Computable maxAvgDuration = new MaxAvgDuration(3);
    Computable maxComputable = new MaxComputable();
    Computable meanComputable = new MeanComputable();
    Computable medianComputable = new MedianComputable();
    Computable minComputable = new MinComputable();
    Computable percientiles = new PercentilesComputable((float) .01);

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void toXML() {

        try {
            var x = maxTest.toXML();
            x = maxAccumDuration.toXML();
            x = maxAvgDuration.toXML();
            x = maxComputable.toXML();
            x = meanComputable.toXML();
            x = medianComputable.toXML();
            x = minComputable.toXML();
            x = percientiles.toXML();
        } catch (Exception exception) {
            exception.printStackTrace();
            fail();
        }

    }

    @Test
    void fromXML() {
        try {
            Computable.fromXML(maxTest.toXML());
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}