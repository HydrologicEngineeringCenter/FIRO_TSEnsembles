package hec.ensemble;

public class TimeSeriesIdentifier {
    public String location;
    public String parameter;

    public TimeSeriesIdentifier(String location, String parameter) {
        this.location = location;
        this.parameter = parameter;
    }
}
