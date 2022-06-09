package hec.stats;

import org.jdom.Element;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MultiComputableTest {

    Statistics[] stats = new Statistics[]{Statistics.MAX, Statistics.MEAN, Statistics.MIN};
    MultiComputable multiStatComputable = new MultiStatComputable(stats);
    MultiComputable cumulativeComputable = new CumulativeComputable();
    MultiComputable percentilesComputable = new PercentilesComputable((float).001);

    @Test
    void toAndFromXML() { ;
        try {
            var a = Serializer.toXML(multiStatComputable);
            var b = Serializer.toXML(cumulativeComputable);
            var c = Serializer.toXML(percentilesComputable);

            var A = Serializer.fromXML(a);
            var B = Serializer.fromXML(b);
            var C = Serializer.fromXML(c);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

    }
}