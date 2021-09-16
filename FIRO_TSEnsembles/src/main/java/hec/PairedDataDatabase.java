package hec;

import hec.paireddata.PairedData;

public interface PairedDataDatabase extends AutoCloseable {
        public abstract PairedData getPairedData(String string);
        public abstract void write(PairedData table);
}
