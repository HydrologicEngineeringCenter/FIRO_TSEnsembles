package hec.stats;

public class NDayMultiComputable implements Computable, StatisticsReportable, Configurable {

    private MultiComputable _stepOne;
    private int _day = 3;
    private int _timesteps = 0;

    public NDayMultiComputable(MultiComputable stepOne, int numberDays) {
        _stepOne = stepOne;
        _day = numberDays;
    }

    @Override
    public float compute(float[] values) {
        values = _stepOne.MultiCompute(values);
        return values[_timesteps];
    }


    @Override
    public void configure(Configuration c) {
     //   _timesteps = c.getDuration(); figure out how many values make up a day and multiple by number of days

    }

    @Override
    public Statistics[] Statistics() {
        return new Statistics[0];
    }
}
