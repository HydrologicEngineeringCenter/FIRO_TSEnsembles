package hec.stats;

public class MultiStatComputable implements MultiComputable{
    Statistics[] statSelection;

    /**
     * The MultiComputable interface is beneficial for creating multiple time series representations.
     * This method iterates across all traces for each timestep to produce multiple values for each
     * timestep. A good example would be the max and min for all timesteps which would represent the
     * bounds of the ensemble.
     * @param statSelection is expected to be a String
     */

    public MultiStatComputable(Statistics[] statSelection) {
        this.statSelection = statSelection;
    }

    @Override
    public float[] MultiCompute(float[] values) {
        int size =  statSelection.length;
        float [] results = new float[size];
        InlineStats is = new InlineStats();
        for(float f : values){
            is.AddObservation(f);
        }

        for (int i = 0; i < size; i++) {

            switch (statSelection[i]){
                case MIN:
                    results[i] = is.getMin();
                    break;
                case MEAN:
                    results[i] = is.getMean();
                    break;
                case MAX:
                    results[i] = is.getMax();
                    break;
                default:
                    throw new ArithmeticException("stat type not  yet supported in MultiStatComputable.");
            }

        }
        return results;
    }
}
