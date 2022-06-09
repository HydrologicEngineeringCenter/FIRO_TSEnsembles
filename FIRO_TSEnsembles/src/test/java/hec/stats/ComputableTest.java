package hec.stats;

import hec.ensemble.Ensemble;
import hec.ensemble.TestData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ComputableTest {
    Computable maxAccumDuration = new MaxAccumDuration(3);
    Computable maxAvgDuration = new MaxAvgDuration(3);
    Computable maxComputable = new MaxComputable();
    Computable meanComputable = new MeanComputable();
    Computable medianComputable = new MedianComputable();
    Computable minComputable = new MinComputable();
    Computable percentiles = new PercentilesComputable((float) .01);

    @Test
    void ToAndFromXML() {
        try {
            var a= Serializer.toXML(maxAccumDuration);
            var b = Serializer.toXML( maxAvgDuration);
            var c = Serializer.toXML( maxComputable);
            var d = Serializer.toXML(meanComputable);
            var e = Serializer.toXML(medianComputable);
            var f = Serializer.toXML(minComputable);
            var g = Serializer.toXML(percentiles);

            var A = Serializer.fromXML(a);
            var B = Serializer.fromXML(b);
            var C = Serializer.fromXML(c);
            var D = Serializer.fromXML(d);
            var E = Serializer.fromXML(e);
            var F = Serializer.fromXML(f);
            var G = Serializer.fromXML(g);

        } catch (Exception exception) {
            exception.printStackTrace();
            fail();
        }
    }

}