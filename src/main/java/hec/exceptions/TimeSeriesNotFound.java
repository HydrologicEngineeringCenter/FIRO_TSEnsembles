package hec.exceptions;

public class TimeSeriesNotFound extends RuntimeException{
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public TimeSeriesNotFound(String msg) {
        super(msg);
    }
}