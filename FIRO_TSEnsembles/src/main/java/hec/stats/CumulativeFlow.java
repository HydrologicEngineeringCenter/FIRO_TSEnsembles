package hec.stats;

public class CumulativeFlow implements Computable, Configurable {
    Configuration _c;
    String _outputFlowUnits;

    /**
     * Instantiates a cumulative flow computable object
     * @param outputFlowUnits this is expected to be units of flow in the output array
     */

    public CumulativeFlow(String outputFlowUnits) {
        this._outputFlowUnits = outputFlowUnits;
    }

    @Override
    public float compute(float[] values) {

        float flowVol = 0;
        for (float Q : values) flowVol += Q;
        return flowVol*unitConverter();
    }

    public float[] computeMulti(float[] values) {
        float[] flowVols = new float[values.length];
        float unitMultiplier = unitConverter();
        flowVols[0] = values[0]*unitMultiplier;
        for (int i = 1; i < values.length; i++) {
            flowVols[i] += flowVols[i-1] + (values[i]*unitMultiplier);
            }
        return flowVols;
    }

    @Override
    public void configure(Configuration c) {
        _c = c;
    }

    private float unitConverter() {
        /*TODO: here we want to check the input and output units to do the conversion*/
        if (_c.getUnits().equals(_outputFlowUnits)){
            return 1.0f;
        }else{
            //don't panic, just guess!
            return 10.0f;
        }
    }

    @Override
    public Statistics[] Statistics() {
        return new Statistics[]{Statistics.CUMULATIVE};
    }
}
