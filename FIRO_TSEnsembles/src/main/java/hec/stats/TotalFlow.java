package hec.stats;

public class TotalFlow implements Computable, Configurable {
    Configuration _c;

    /**
     * Instantiates a total flow computable object
     */

    public TotalFlow() {
    }

    @Override
    public float compute(float[] values) {

        float flowVol = 0;
        for (float Q : values) flowVol += Q;
        return flowVol;
    }

    @Override
    public void configure(Configuration c) {
        _c = c;
    }


    @Override
    public Statistics[] Statistics() {
        return new Statistics[]{Statistics.TOTAL};
    }
}
