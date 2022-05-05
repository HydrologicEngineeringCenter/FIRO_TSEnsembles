package hec.stats;

public class NDayMultiComputable implements Computable, StatisticsReportable, Configurable {
    /**
     * The n day multi computable computes a multiComputable stat (cumulative) and gets the cumulative value for the specified day.
     * Intended to be used for iterating for traces across time
     * @param numberDays this is expected to be in integer days
     */

    private MultiComputable _stepOne;
    private int _day;
    Configuration _c;

    public NDayMultiComputable(MultiComputable stepOne, int numberDays) {
        _stepOne = stepOne;
        _day = numberDays;
    }

    @Override
    public float compute(float[] values) {
        values = _stepOne.MultiCompute(values);
        int timestep = (int) _c.getDuration().toHours();
        int timestepDay = timestep * 24;
        return values[timestepDay * _day];
    }


    @Override
    public void configure(Configuration c) {
        _c= c; //figure out how many values make up a day and multiple by number of days
    }

    @Override
    public Statistics[] Statistics() {
        return new Statistics[0];
    }
}
