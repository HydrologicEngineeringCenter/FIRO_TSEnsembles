package hec.stats;

public class MultiStatComputable implements MultiComputable{
    String[] statSelection;

    /**
     * The MultiComputable interface is beneficial for creating multiple time series representations.
     * This method iterates across all traces for each timestep to produce multiple values for each
     * timestep. A good example would be the max and min for all timesteps which would represent the
     * bounds of the ensemble.
     * @param statSelection is expected to be a String
     */

    public MultiStatComputable(String[] statSelection) {
        this.statSelection = statSelection;
        for (String s : this.statSelection) {
            if (s.equals("MIN")) {
                break;
            }
            if (s.equals("AVERAGE")) {
                break;
            }
            if (s.equals("MEDIAN")) {
                break;
            } else {
                throw new ArithmeticException("Please select from one of the available statistics");
            }
        }
    }

    @Override
    public float[] MultiCompute(float[] values) {
        int size =  statSelection.length;
        float [] results = new float[size];
        MultiStat arr [] = MultiStat.values();

        for (int i = 0; i < this.statSelection.length; i++) {
            MultiStat enumType = MultiStat.valueOf(statSelection[i]);
            float test = createCalculation(enumType).compute(values);
            results[i] = test;
        }
        return results;
    }


    public Computable createCalculation(MultiStat stat) {
        Computable statValue = null;
        switch(stat) {
            case MIN:
                statValue = new MinComputable();
                break;
            case MEDIAN:
                statValue = new MedianComputable();
                break;
            case AVERAGE:
                statValue = new MeanComputable();
                break;
        }
        return statValue;
    }
}
