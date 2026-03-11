package com.clfacv.loader.parser;

import com.clfacv.loader.model.FacvRecord;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class FixedWidthFacvParserTest {

    private final FixedWidthFacvParser parser = new FixedWidthFacvParser();

    @Test
    void parseLine_shouldMapAndTrimFields() {
        String line = "004"
                + "065017162001"
                + "CIF"
                + " "
                + "C"
                + "00000000072"
                + "    00000  "
                + "DOD01"
                + "00000000263"
                + " A B   "
                + "D";

        FacvRecord record = parser.parseLine(line);

        assertEquals("004", record.getBnkNo());
        assertEquals("065017162001", record.getCustAcctNo());
        assertEquals("CIF", record.getSysCod());
        assertNull(record.getRecType());
        assertEquals("C", record.getCustGp());
        assertEquals("00000000072", record.getItlCustNo());
        assertEquals("00000", record.getFiller());
        assertEquals("DOD01", record.getLmtId());
        assertEquals("00000000263", record.getCustId());
        assertEquals("A B", record.getFiller1());
        assertEquals("D", record.getMaintAct());
    }

    @Test
    void parseLine_shouldReturnNullForMissingTail() {
        String line = "004065017162001";

        FacvRecord record = parser.parseLine(line);

        assertEquals("004", record.getBnkNo());
        assertEquals("065017162001", record.getCustAcctNo());
        assertNull(record.getSysCod());
        assertNull(record.getMaintAct());
    }
}
