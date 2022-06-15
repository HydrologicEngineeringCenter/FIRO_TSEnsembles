package hec;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (o instanceof RecordIdentifier) {
            RecordIdentifier other = (RecordIdentifier) o;
            return Objects.equals(other.parameter, this.parameter) && Objects.equals(other.location, this.location);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (toString()).hashCode();
    }
}
