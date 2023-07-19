package hec;

import java.util.Objects;

public class VersionIdentifier {

    public String version;
    public String parameter;
    public String location;

    public VersionIdentifier(String location, String parameter, String version) {
        this.location = location;
        this.parameter = parameter;
        this.version = version;
    }

    @Override
    public String toString()
    {
        return location+"/"+parameter+"/"+version;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof VersionIdentifier) {
            VersionIdentifier other = (VersionIdentifier) o;
            return Objects.equals(other.parameter, this.parameter) && Objects.equals(other.location, this.location)
                    && Objects.equals(other.version, this.version);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (toString()).hashCode();
    }
}
