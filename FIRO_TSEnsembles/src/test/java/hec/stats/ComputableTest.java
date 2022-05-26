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
        maxTest.toXML();
        maxAccumDuration.toXML();
        maxAvgDuration.toXML();
        maxComputable.toXML();
        meanComputable.toXML();
        medianComputable.toXML();
        minComputable.toXML();
        percientiles.toXML();
    }

    @Test
    void fromXML() {
        Computable.fromXML(maxTest.toXML());
    }
}