package hec.dss.ensemble;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CatalogTest {

    @Test
    void extractVersionLabel_returnsUntaggedSegment() {
        assertEquals("HEFS", Catalog.extractVersionLabel("C:000001|HEFS"));
    }

    @Test
    void extractVersionLabel_returnsEmptyWhenAllSegmentsAreTagged() {
        assertEquals("", Catalog.extractVersionLabel("C:000007|T:20131103-1200|V:20131103-120000|"));
    }

    @Test
    void extractVersionLabel_returnsFirstUntaggedSegment() {
        assertEquals("MyLabel", Catalog.extractVersionLabel("C:000001|MyLabel|AnotherLabel"));
    }

    @Test
    void extractVersionLabel_returnsEmptyForNull() {
        assertEquals("", Catalog.extractVersionLabel(null));
    }

    @Test
    void extractVersionLabel_returnsEmptyForEmptyString() {
        assertEquals("", Catalog.extractVersionLabel(""));
    }

    @Test
    void extractVersionLabel_returnsEmptyWhenOnlyTaggedSegment() {
        assertEquals("", Catalog.extractVersionLabel("C:000001"));
    }
}