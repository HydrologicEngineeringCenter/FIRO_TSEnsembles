package hec.stats;

public class InlineStats {
    private float _min;
    private float _max;
    private int _sampleSize;
    private float _mean;
    private float _sampleVariance;
    public float getMin(){
        return _min;
    }
    public float getMax(){
        return _max;
    }
    public float getMean(){
        return _mean;
    }
    public int getSampleSize(){
        return _sampleSize;
    }
    public float getSampleVariance(){
        return _sampleVariance;
    }
    public float getSampleStandardDeviation() {return (float) Math.sqrt(_sampleVariance); }
    public InlineStats(){
        //no initialization logic necessary.
    }
    public void AddObservation(float value){
        float sampleVariance = 0;
        float part1 = 0;
        float part2 = 0;
        if (_sampleSize == 0){
            _min = value;
            _max = value;
            _mean = value;
            _sampleVariance = 0;
            _sampleSize = 1;
        }else{
            if (value>_max){
                _max = value;
            }else if(value < _min){
                _min = value;
            }
            _sampleSize +=1;

            _sampleVariance = ((float)(_sampleSize - 2) / (_sampleSize - 1)) * _sampleVariance + (float) Math.pow(value-_mean,2)/(float)(_sampleSize);
            _mean += ((value - _mean)/(float)(_sampleSize));

        }
    }
}
