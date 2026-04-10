package hec.ensemble.stats;

import org.jdom.Element;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

class SingleValueComputableTest {
    SingleValueComputable maxOfMax = new MaxOfMaximumsValueComputable();
    Statistics[] stats = new Statistics[]{Statistics.MAX};
    MultiComputable mc = new MultiStatComputable(stats);
    NDayMultiComputable nday = new NDayMultiComputable(mc,new float[] {2});
    SingleValueComputable twostep = new TwoStepComputableSingleMetricValue(nday,new MeanComputable(), true);

    @Test
    void ToAndFromXML() {
        try {
            Element a= Serializer.toXML(maxOfMax);
            Element b = Serializer.toXML( twostep);

            SingleValueComputable A = Serializer.fromXML(a);
            SingleValueComputable B = Serializer.fromXML(b);

            System.out.println("uhhh");

        } catch (Exception exception) {
            exception.printStackTrace();
            fail();
        }
    }
}