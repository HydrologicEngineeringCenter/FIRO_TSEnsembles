package hec.ensemble;

import java.time.Duration;
import java.time.ZonedDateTime;
import hec.stats.Computable;
import hec.stats.Configurable;

/**
 * an Ensemble is an array of time-series data
 *
 * each time-series in the Ensemble has the same timestamps
 *
 */
public class Ensemble
  {
    private final EnsembleConfiguration _configuration;
    protected EnsembleTimeSeries parent = null;


    public Ensemble(ZonedDateTime issueDate, float[][] values, ZonedDateTime startDate, Duration interval)
    {
      this.values = values;
      this._configuration = new EnsembleConfiguration(issueDate,startDateTime,interval);
    }

    public int getTimeCount(){
      return values[0].length;
  }

    /**
     * Computes an array of ZonedDateTime based on the
     * interval and startDateTime of the ensemble.
     * @return returns array of ZonedDateTime
     */
    public ZonedDateTime[] startDateTime() {

      ZonedDateTime[] rval = new ZonedDateTime[getTimeCount()];
      ZonedDateTime t = startDateTime;
      int size= getTimeCount();
      for (int i = 0; i <size ; i++) {
        rval[i] =t;
        t.plus(interval);
      }
      return rval;
    }

    private float[][] values;


    public ZonedDateTime getIssueDate() {
      return _configuration.getIssueDate();
    }

    public Duration getInterval()
    {
      return _configuration.getDuration();
    }

    public ZonedDateTime getStartDateTime() {
      return _configuration.getStartDateTime();
    }

    /**
     * ensemble data
     * row represents ensemble members
     * columns are time steps
     * @return tow-dimensional zize float[][] (all rows are the same size)
     */
    public float[][] getValues() {
      return values;
    }
    public float[] iterateForTracesAcrossTime(Computable cmd){
      if (cmd instanceof hec.stats.Configurable){
        ((Configurable)cmd).configure(_configuration);
      }
      int size= values.length;
      float[] rval = new float[size];
      for (int i = 0; i <size ; i++) {
          rval[i] = cmd.compute(values[i]);
      }
      return rval;
    }
    public float[] iterateForTimeAcrossTraces(Computable cmd){
      if (cmd instanceof hec.stats.Configurable){
        ((Configurable)cmd).configure(_configuration);
      }
      int size= values[0].length;
      float[] rval = new float[size];
      int traces = values.length;
      float[] tracevals = new float[traces];
      for (int i = 0; i <size ; i++) {//this could be more efficent as a streaming compute process.. one less loop.
        for(int j = 0; i <traces; j++){
          tracevals[j] = values[j][i];
        }
        rval[i] = cmd.compute(tracevals);
      }
      return rval;
    }
  }
