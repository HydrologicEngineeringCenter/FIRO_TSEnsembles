package hec.ensemble.stats;

public class NDayMultiComputable implements Computable, StatisticsReportable, Configurable {

    private  MultiComputable _stepOne;
    private  int _day;
    Configuration _c;

    /**
     * The n day multi computable computes a multiComputable stat (cumulative) and gets the cumulative value for the specified day.
     * Intended to be used for iterating for traces across time and does not account for daylight savings
     */
    public NDayMultiComputable(){ } // necessary for reflection deserializing serializing
    public NDayMultiComputable(MultiComputable stepOne, int numberDays) {
        _stepOne = stepOne;
        _day = numberDays;
    }

    @Override
    public float compute(float[] values) {
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
    public Statistics[] Statistics()
    {
        Statistics[] statsarray = new Statistics[_stepOne.Statistics().length+1];
        statsarray[0] = Statistics.CUMULATIVE;
        int count = 1;
        for(Statistics s: _stepOne.Statistics()){
            statsarray[count] = s;
            count++;
        }
        return new Statistics[0];
    }

    @Override
    public String StatisticsLabel() {
        return _day+"day"+_stepOne.StatisticsLabel();
    }


}

