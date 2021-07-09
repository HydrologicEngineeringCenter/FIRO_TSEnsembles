package hec.stats;

public class MultiStatComputable implements MultiComputable{
    String[] statSelection;

    /**
     * Can instantiate multiple computable objects within the stats library.
     * @param statSelection is expected to be a String
     */

    public MultiStatComputable(String[] statSelection) {
        this.statSelection = statSelection;
        for(int i = 0; i < this.statSelection.length; i++) {
            if (this.statSelection[i] != "MIN" || this.statSelection[i] != "AVERAGE" || this.statSelection[i] != "MEDIAN") {
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
