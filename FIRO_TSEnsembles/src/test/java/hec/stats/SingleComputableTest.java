package hec.stats;

import hec.ensemble.stats.*;
import org.jdom.Element;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SingleComputableTest {
    SingleComputable maxOfMax = new MaxOfMaximumsComputable();
    SingleComputable twostep = new TwoStepComputable(new MaxComputable(),new MeanComputable(), true);

    @Test
    void ToAndFromXML() {
        try {
            Element a= Serializer.toXML(maxOfMax);
            Element b = Serializer.toXML( twostep);

            SingleComputable A = Serializer.fromXML(a);
            SingleComputable B = Serializer.fromXML(b);

        } catch (Exception exception) {
            exception.printStackTrace();
            fail();
        }
    }
}