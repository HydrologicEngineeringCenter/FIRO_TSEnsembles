package hec.stats;

public class NDayMultiComputable implements Computable, StatisticsReportable, Configurable {

    private final MultiComputable _stepOne;
    private final int _day;
    Configuration _c;

    /**
     * The n day multi computable computes a multiComputable stat (cumulative) and gets the cumulative value for the specified day.
     * Intended to be used for iterating for traces across time and does not account for daylight savings
     * @param numberDays this is expected to be in integer days
     */

    public NDayMultiComputable(MultiComputable stepOne, int numberDays) {
        _stepOne = stepOne;
        _day = numberDays;
    }

    @Override
    public float compute(float[] values) {
        if (_stepOne instanceof hec.stats.Configurable && _c != null){
            ((Configurable)_stepOne).configure(_c);
        }

        values = _stepOne.multiCompute(values);
        int timestep = (int) _c.getDuration().toHours();
        int timestepDay = 24 / timestep;
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
