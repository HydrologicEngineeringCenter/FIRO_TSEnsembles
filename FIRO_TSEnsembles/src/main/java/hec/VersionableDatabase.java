package hec;

import java.util.List;

public interface VersionableDatabase extends AutoCloseable {
    public abstract String getVersion();
    public abstract List<String> getVersions();
    public abstract String getUpdateScript(String from, String to);
}
