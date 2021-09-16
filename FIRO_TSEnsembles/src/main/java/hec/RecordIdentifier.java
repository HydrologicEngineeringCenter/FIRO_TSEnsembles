package hec;

public class RecordIdentifier {
    public String location;
    public String parameter;

    public RecordIdentifier(String location, String parameter) {
        this.location = location;
        this.parameter = parameter;
    }

    @Override
    public String toString()
    {
        return location+"/"+parameter;
    }
}
