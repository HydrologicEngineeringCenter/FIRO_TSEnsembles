package hec;

import java.util.Objects;

public class RecordIdentifier {
    public String location;
    public String parameter;
    public String version;

    public RecordIdentifier(String location, String parameter) {
        this(location, parameter, "");
    }

    public RecordIdentifier(String location, String parameter, String version) {
        this.location = location;
        this.parameter = parameter;
        this.version = version != null ? version : "";
    }

    @Override
    public String toString()
    {
        if (version.isEmpty()) {
            return location + "/" + parameter;
        }
        return location + "/" + parameter + "/" + version;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof RecordIdentifier) {
            RecordIdentifier other = (RecordIdentifier) o;
            return Objects.equals(other.parameter, this.parameter)
                    && Objects.equals(other.location, this.location)
                    && Objects.equals(other.version, this.version);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (toString()).hashCode();
    }
}
