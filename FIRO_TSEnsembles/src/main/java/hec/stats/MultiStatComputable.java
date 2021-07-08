package hec.stats;

public class MultiStatComputable {

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
