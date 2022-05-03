package hec.stats;

public class NDayMultiComputable implements Computable, StatisticsReportable, Configurable {

    private MultiComputable _stepOne;
    private int _day;
    Configuration _c;
    private int _timesteps = 0;

    public NDayMultiComputable(MultiComputable stepOne, int numberDays) {
        _stepOne = stepOne;
        _day = numberDays;
    }

    @Override
    public float compute(float[] values) {
        values = _stepOne.MultiCompute(values);
        int timestep = (int) _c.getDuration().toHours();
        int timestepDay = timestep / 24;
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
