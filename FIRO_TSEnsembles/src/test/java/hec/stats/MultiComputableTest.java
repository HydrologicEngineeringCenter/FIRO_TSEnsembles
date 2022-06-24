package hec.stats;

import hec.ensemble.stats.*;
import org.jdom.Element;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MultiComputableTest {

    Statistics[] stats = new Statistics[]{Statistics.MAX, Statistics.AVERAGE, Statistics.MIN};
    MultiComputable multiStatComputable = new MultiStatComputable(stats);
    MultiComputable cumulativeComputable = new CumulativeComputable();
    MultiComputable percentilesComputable = new PercentilesComputable((float).001);

    @Test
    void toAndFromXML() { ;
        try {
            Element a = Serializer.toXML(multiStatComputable);
            Element b = Serializer.toXML(cumulativeComputable);
            Element c = Serializer.toXML(percentilesComputable);

            MultiComputable A = Serializer.fromXML(a);
            MultiComputable B = Serializer.fromXML(b);
            MultiComputable C = Serializer.fromXML(c);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

    }
}