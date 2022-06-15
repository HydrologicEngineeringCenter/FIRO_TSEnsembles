package hec.stats;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SingleComputableTest {
    SingleComputable maxOfMax = new MaxOfMaximumsComputable();
    SingleComputable twostep = new TwoStepComputable(new MaxComputable(),new MeanComputable(), true);

    @Test
    void ToAndFromXML() {
        try {
            var a= Serializer.toXML(maxOfMax);
            var b = Serializer.toXML( twostep);

            var A = Serializer.fromXML(a);
            var B = Serializer.fromXML(b);

        } catch (Exception exception) {
            exception.printStackTrace();
            fail();
        }
    }
}