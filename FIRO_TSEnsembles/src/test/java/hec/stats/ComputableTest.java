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
            var x = Computable.fromXML(maxTest.toXML());
            x= Computable.fromXML(maxAccumDuration.toXML());
            x = Computable.fromXML(maxAvgDuration.toXML());
            x = Computable.fromXML(maxComputable.toXML());
            x = Computable.fromXML(meanComputable.toXML());
            x = Computable.fromXML(medianComputable.toXML());
            x = Computable.fromXML(medianComputable.toXML());
            x = Computable.fromXML(minComputable.toXML());
            x = Computable.fromXML(percientiles.toXML());
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}