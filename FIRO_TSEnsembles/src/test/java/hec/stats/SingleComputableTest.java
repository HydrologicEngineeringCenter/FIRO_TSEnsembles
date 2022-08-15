package hec.stats;

import hec.ensemble.stats.*;
import org.jdom.Element;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SingleComputableTest {
    SingleComputable maxOfMax = new MaxOfMaximumsComputable();
    Statistics[] stats = new Statistics[]{Statistics.MAX};
    MultiComputable mc = new MultiStatComputable(stats);
    NDayMultiComputable nday = new NDayMultiComputable(mc,2);
    SingleComputable twostep = new TwoStepComputable(nday,new MeanComputable(), true);

    @Test
    void ToAndFromXML() {
        try {
            Element a= Serializer.toXML(maxOfMax);
            Element b = Serializer.toXML( twostep);

            SingleComputable A = Serializer.fromXML(a);
            SingleComputable B = Serializer.fromXML(b);

            System.out.println("uhhh");

        } catch (Exception exception) {
            exception.printStackTrace();
            fail();
        }
    }
}