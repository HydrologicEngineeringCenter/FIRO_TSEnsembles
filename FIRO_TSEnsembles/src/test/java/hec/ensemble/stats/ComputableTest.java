package hec.ensemble.stats;

import hec.ensemble.stats.*;
import org.jdom.Element;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

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
            Element a= Serializer.toXML(maxAccumDuration);
            Element b = Serializer.toXML( maxAvgDuration);
            Element c = Serializer.toXML( maxComputable);
            Element d = Serializer.toXML(meanComputable);
            Element e = Serializer.toXML(medianComputable);
            Element f = Serializer.toXML(minComputable);
            Element g = Serializer.toXML(percentiles);

            Computable A = Serializer.fromXML(a);
            Computable B = Serializer.fromXML(b);
            Computable C = Serializer.fromXML(c);
            Computable D = Serializer.fromXML(d);
            Computable E = Serializer.fromXML(e);
            Computable F = Serializer.fromXML(f);
            Computable G = Serializer.fromXML(g);

        } catch (Exception exception) {
            exception.printStackTrace();
            fail();
        }
    }

}