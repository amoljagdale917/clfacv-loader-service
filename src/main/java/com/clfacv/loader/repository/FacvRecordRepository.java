package com.clfacv.loader.repository;

import com.clfacv.loader.model.FacvRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

@Repository
public class FacvRecordRepository {

    private static final String INSERT_SQL =
            "INSERT INTO STG_HK_OBS_FACV (" +
                    "BNK_NO, CUST_ACCT_NO, SYS_COD, REC_TYPE, CUST_GP, " +
                    "ITL_CUST_NO, FILLER, LMT_ID, CUST_ID, FILLER1, MAINT_ACT" +
                    ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private final JdbcTemplate jdbcTemplate;

    public FacvRecordRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void saveBatch(List<FacvRecord> records) {
        if (records == null || records.isEmpty()) {
            return;
        }

        jdbcTemplate.batchUpdate(INSERT_SQL, records, records.size(), this::mapRecord);
    }

    private void mapRecord(PreparedStatement ps, FacvRecord record) throws SQLException {
        setNullableString(ps, 1, record.getBnkNo());
        setNullableString(ps, 2, record.getCustAcctNo());
        setNullableString(ps, 3, record.getSysCod());
        setNullableString(ps, 4, record.getRecType());
        setNullableString(ps, 5, record.getCustGp());
        setNullableString(ps, 6, record.getItlCustNo());
        setNullableString(ps, 7, record.getFiller());
        setNullableString(ps, 8, record.getLmtId());
        setNullableString(ps, 9, record.getCustId());
        setNullableString(ps, 10, record.getFiller1());
        setNullableString(ps, 11, record.getMaintAct());
    }

    private void setNullableString(PreparedStatement ps, int index, String value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.VARCHAR);
        } else {
            ps.setString(index, value);
        }
    }
}
