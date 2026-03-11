package com.clfacv.loader.parser;

import com.clfacv.loader.model.FacvRecord;
import org.springframework.stereotype.Component;

@Component
public class FixedWidthFacvParser {

    private static final int BNK_NO_LEN = 3;
    private static final int CUST_ACCT_NO_LEN = 12;
    private static final int SYS_COD_LEN = 3;
    private static final int REC_TYPE_LEN = 1;
    private static final int CUST_GP_LEN = 1;
    private static final int ITL_CUST_NO_LEN = 11;
    private static final int FILLER_LEN = 11;
    private static final int LMT_ID_LEN = 5;
    private static final int CUST_ID_LEN = 11;
    private static final int FILLER1_LEN = 7;
    private static final int MAINT_ACT_LEN = 1;

    public FacvRecord parseLine(String line) {
        int offset = 0;

        FacvRecord record = new FacvRecord();
        record.setBnkNo(extractAndTrim(line, offset, BNK_NO_LEN));
        offset += BNK_NO_LEN;

        record.setCustAcctNo(extractAndTrim(line, offset, CUST_ACCT_NO_LEN));
        offset += CUST_ACCT_NO_LEN;

        record.setSysCod(extractAndTrim(line, offset, SYS_COD_LEN));
        offset += SYS_COD_LEN;

        record.setRecType(extractAndTrim(line, offset, REC_TYPE_LEN));
        offset += REC_TYPE_LEN;

        record.setCustGp(extractAndTrim(line, offset, CUST_GP_LEN));
        offset += CUST_GP_LEN;

        record.setItlCustNo(extractAndTrim(line, offset, ITL_CUST_NO_LEN));
        offset += ITL_CUST_NO_LEN;

        record.setFiller(extractAndTrim(line, offset, FILLER_LEN));
        offset += FILLER_LEN;

        record.setLmtId(extractAndTrim(line, offset, LMT_ID_LEN));
        offset += LMT_ID_LEN;

        record.setCustId(extractAndTrim(line, offset, CUST_ID_LEN));
        offset += CUST_ID_LEN;

        record.setFiller1(extractAndTrim(line, offset, FILLER1_LEN));
        offset += FILLER1_LEN;

        record.setMaintAct(extractAndTrim(line, offset, MAINT_ACT_LEN));

        return record;
    }

    private String extractAndTrim(String line, int start, int length) {
        if (line == null || line.length() <= start) {
            return null;
        }

        int end = Math.min(start + length, line.length());
        String raw = line.substring(start, end);
        String trimmed = raw.trim();

        return trimmed.isEmpty() ? null : trimmed;
    }
}
