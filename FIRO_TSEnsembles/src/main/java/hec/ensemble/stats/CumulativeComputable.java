package hec.ensemble.stats;

public class CumulativeComputable implements MultiComputable {
    private float[] _cumulative;

    /**
     * Instantiates a cumulative computable object
     */

    public CumulativeComputable() {
    }


    @Override
    public float[] multiCompute(float[] values) {
        float[] flowVol = new float[values.length];
        for (int i = 0; i < flowVol.length; i++) {
            if(i == 0) {
                flowVol[i] = values[0];
            } else {
                flowVol[i] = flowVol[i - 1] + values[i];
            }
        }
        return flowVol;
    }

    @Override
    public Statistics[] Statistics() {
        return new Statistics[]{Statistics.CUMULATIVE};
    }
}
