package com.clfacv.loader.config;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class FixedWidthLayoutRegistry {

    private static final String CLFACV = "CLFACV";
    private static final String CLFACVHASE = "CLFACVHASE";
    private static final String CLIMTM = "CLIMTM";

    private static final Map<String, List<LayoutColumn>> LAYOUT_BY_FILE;

    static {
        Map<String, List<LayoutColumn>> layouts = new HashMap<String, List<LayoutColumn>>();
        List<LayoutColumn> facvLayout = facvLayout();
        List<LayoutColumn> imtmLayout = imtmLayout();
        layouts.put(CLFACV, facvLayout);
        layouts.put(CLFACVHASE, facvLayout);
        layouts.put(CLIMTM, imtmLayout);
        LAYOUT_BY_FILE = Collections.unmodifiableMap(layouts);
    }

    public List<LoaderProperties.ColumnDefinition> getColumnsForFile(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return Collections.emptyList();
        }

        String key = normalizeFileKey(fileName);
        List<LayoutColumn> columns = LAYOUT_BY_FILE.get(key);
        if (columns == null || columns.isEmpty()) {
            return Collections.emptyList();
        }

        List<LoaderProperties.ColumnDefinition> resolved = new ArrayList<LoaderProperties.ColumnDefinition>(columns.size());
        for (LayoutColumn column : columns) {
            LoaderProperties.ColumnDefinition next = new LoaderProperties.ColumnDefinition();
            next.setName(column.getName());
            next.setLength(column.getLength());
            resolved.add(next);
        }

        return resolved;
    }

    private static List<LayoutColumn> facvLayout() {
        List<LayoutColumn> columns = new ArrayList<LayoutColumn>();
        columns.add(new LayoutColumn("BNK_NO", 3));
        columns.add(new LayoutColumn("CUST_ACCT_NO", 12));
        columns.add(new LayoutColumn("SYS_COD", 3));
        columns.add(new LayoutColumn("REC_TYPE", 1));
        columns.add(new LayoutColumn("CUST_GP", 1));
        columns.add(new LayoutColumn("ITL_CUST_NO", 11));
        columns.add(new LayoutColumn("FILLER", 11));
        columns.add(new LayoutColumn("LMT_ID", 5));
        columns.add(new LayoutColumn("CUST_ID", 11));
        columns.add(new LayoutColumn("FILLER1", 7));
        columns.add(new LayoutColumn("MAINT_ACT", 1));
        return Collections.unmodifiableList(columns);
    }

    private static List<LayoutColumn> imtmLayout() {
        List<LayoutColumn> columns = new ArrayList<LayoutColumn>();
        columns.add(new LayoutColumn("TREE_ID", 24));
        columns.add(new LayoutColumn("LMT_ID", 5));
        columns.add(new LayoutColumn("MAINT_ACT", 1));
        columns.add(new LayoutColumn("MAX_BP_BD_TERM", 4));
        columns.add(new LayoutColumn("MAX_PC_TERM", 4));
        columns.add(new LayoutColumn("MAX_PC_CR", 4));
        columns.add(new LayoutColumn("NT_STD_INT_RT_FLG", 10));
        columns.add(new LayoutColumn("INT_BOOK_REC", 30));
        columns.add(new LayoutColumn("NT_STD_COMM_FLG", 1));
        columns.add(new LayoutColumn("REMK_FULL", 400));
        columns.add(new LayoutColumn("CILE_CCY", 3));
        columns.add(new LayoutColumn("CILE_MIN_AMT_SIGN", 1));
        columns.add(new LayoutColumn("CILE_MIN_AMT_VALUE", 11));
        columns.add(new LayoutColumn("CILE_MIN_AMT_DCML", 1));
        columns.add(new LayoutColumn("CILE_TIER_AMT_1_SIGN", 1));
        columns.add(new LayoutColumn("CILE_TIER_AMT_1_VALUE", 11));
        columns.add(new LayoutColumn("CILE_TIER_AMT_1_DCML", 1));
        columns.add(new LayoutColumn("CILE_TIER_AMT_2_SIGN", 1));
        columns.add(new LayoutColumn("CILE_TIER_AMT_2_VALUE", 11));
        columns.add(new LayoutColumn("CILE_TIER_AMT_2_DCML", 1));
        columns.add(new LayoutColumn("CILE_TIER_AMT_3_SIGN", 1));
        columns.add(new LayoutColumn("CILE_TIER_AMT_3_VALUE", 11));
        columns.add(new LayoutColumn("CILE_TIER_AMT_3_DCML", 1));
        columns.add(new LayoutColumn("CILE_TIER_AMT_4_SIGN", 1));
        columns.add(new LayoutColumn("CILE_TIER_AMT_4_VALUE", 11));
        columns.add(new LayoutColumn("CILE_TIER_AMT_4_DCML", 1));
        columns.add(new LayoutColumn("CILE_TIER_AMT_5_SIGN", 1));
        columns.add(new LayoutColumn("CILE_TIER_AMT_5_VALUE", 11));
        columns.add(new LayoutColumn("CILE_TIER_AMT_5_DCML", 1));
        columns.add(new LayoutColumn("CILE_TIER_RT_1_SIGN", 1));
        columns.add(new LayoutColumn("CILE_TIER_RT_1_VALUE", 11));
        columns.add(new LayoutColumn("CILE_TIER_RT_1_DCML", 1));
        columns.add(new LayoutColumn("CILE_TIER_RT_2_SIGN", 1));
        columns.add(new LayoutColumn("CILE_TIER_RT_2_VALUE", 11));
        columns.add(new LayoutColumn("CILE_TIER_RT_2_DCML", 1));
        columns.add(new LayoutColumn("CILE_TIER_RT_3_SIGN", 1));
        columns.add(new LayoutColumn("CILE_TIER_RT_3_VALUE", 11));
        columns.add(new LayoutColumn("CILE_TIER_RT_3_DCML", 1));
        columns.add(new LayoutColumn("CILE_TIER_RT_4_SIGN", 1));
        columns.add(new LayoutColumn("CILE_TIER_RT_4_VALUE", 11));
        columns.add(new LayoutColumn("CILE_TIER_RT_4_DCML", 1));
        columns.add(new LayoutColumn("CILE_TIER_RT_5_SIGN", 1));
        columns.add(new LayoutColumn("CILE_TIER_RT_5_VALUE", 11));
        columns.add(new LayoutColumn("CILE_TIER_RT_5_DCML", 1));
        columns.add(new LayoutColumn("CILE_TIER_RT_6_SIGN", 1));
        columns.add(new LayoutColumn("CILE_TIER_RT_6_VALUE", 11));
        columns.add(new LayoutColumn("CILE_TIER_RT_6_DCML", 1));
        columns.add(new LayoutColumn("CHRG_COMM_REC_1", 149));
        columns.add(new LayoutColumn("FINC_RT_1_SIGN", 1));
        columns.add(new LayoutColumn("FINC_RT_1_VALUE", 11));
        columns.add(new LayoutColumn("FINC_RT_1_DCML", 1));
        columns.add(new LayoutColumn("FINC_RT_2_SIGN", 1));
        columns.add(new LayoutColumn("FINC_RT_2_VALUE", 11));
        columns.add(new LayoutColumn("FINC_RT_2_DCML", 1));
        columns.add(new LayoutColumn("FINC_RT_3_SIGN", 1));
        columns.add(new LayoutColumn("FINC_RT_3_VALUE", 11));
        columns.add(new LayoutColumn("FINC_RT_3_DCML", 1));
        columns.add(new LayoutColumn("FINC_RT_4_SIGN", 1));
        columns.add(new LayoutColumn("FINC_RT_4_VALUE", 11));
        columns.add(new LayoutColumn("FINC_RT_4_DCML", 1));
        columns.add(new LayoutColumn("FINC_PR_1_SIGN", 1));
        columns.add(new LayoutColumn("FINC_PR_1_VALUE", 3));
        columns.add(new LayoutColumn("FINC_PR_2_SIGN", 1));
        columns.add(new LayoutColumn("FINC_PR_2_VALUE", 3));
        columns.add(new LayoutColumn("FINC_PR_3_SIGN", 1));
        columns.add(new LayoutColumn("FINC_PR_3_VALUE", 3));
        columns.add(new LayoutColumn("FINC_PR_4_SIGN", 1));
        columns.add(new LayoutColumn("FINC_PR_4_VALUE", 3));
        columns.add(new LayoutColumn("FINC_AMT_1_SIGN", 1));
        columns.add(new LayoutColumn("FINC_AMT_1_VALUE", 11));
        columns.add(new LayoutColumn("FINC_AMT_1_DCML", 1));
        columns.add(new LayoutColumn("FINC_AMT_2_SIGN", 1));
        columns.add(new LayoutColumn("FINC_AMT_2_VALUE", 11));
        columns.add(new LayoutColumn("FINC_AMT_2_DCML", 1));
        columns.add(new LayoutColumn("FINC_AMT_3_SIGN", 1));
        columns.add(new LayoutColumn("FINC_AMT_3_VALUE", 11));
        columns.add(new LayoutColumn("FINC_AMT_3_DCML", 1));
        columns.add(new LayoutColumn("FINC_AMT_4_SIGN", 1));
        columns.add(new LayoutColumn("FINC_AMT_4_VALUE", 11));
        columns.add(new LayoutColumn("FINC_AMT_4_DCML", 1));
        columns.add(new LayoutColumn("FINC_CCY_UPR_LWR_LMT", 3));
        columns.add(new LayoutColumn("FINC_UPR_LMT_SIGN", 1));
        columns.add(new LayoutColumn("FINC_UPR_LMT_VALUE", 11));
        columns.add(new LayoutColumn("FINC_UPR_LMT_DCML", 1));
        columns.add(new LayoutColumn("FINC_LWR_LMT_SIGN", 1));
        columns.add(new LayoutColumn("FINC_LWR_LMT_VALUE", 11));
        columns.add(new LayoutColumn("FINC_LWR_LMT_DCML", 1));
        columns.add(new LayoutColumn("CLLTN_RT_1_SIGN", 1));
        columns.add(new LayoutColumn("CLLTN_RT_1_VALUE", 11));
        columns.add(new LayoutColumn("CLLTN_RT_1_DCML", 1));
        columns.add(new LayoutColumn("CLLTN_RT_2_SIGN", 1));
        columns.add(new LayoutColumn("CLLTN_RT_2_VALUE", 11));
        columns.add(new LayoutColumn("CLLTN_RT_2_DCML", 1));
        columns.add(new LayoutColumn("CLLTN_RT_3_SIGN", 1));
        columns.add(new LayoutColumn("CLLTN_RT_3_VALUE", 11));
        columns.add(new LayoutColumn("CLLTN_RT_3_DCML", 1));
        columns.add(new LayoutColumn("CLLTN_RT_4_SIGN", 1));
        columns.add(new LayoutColumn("CLLTN_RT_4_VALUE", 11));
        columns.add(new LayoutColumn("CLLTN_RT_4_DCML", 1));
        columns.add(new LayoutColumn("CLLTN_PR_1_SIGN", 1));
        columns.add(new LayoutColumn("CLLTN_PR_1_VALUE", 3));
        columns.add(new LayoutColumn("CLLTN_PR_2_SIGN", 1));
        columns.add(new LayoutColumn("CLLTN_PR_2_VALUE", 3));
        columns.add(new LayoutColumn("CLLTN_PR_3_SIGN", 1));
        columns.add(new LayoutColumn("CLLTN_PR_3_VALUE", 3));
        columns.add(new LayoutColumn("CLLTN_PR_4_SIGN", 1));
        columns.add(new LayoutColumn("CLLTN_PR_4_VALUE", 3));
        columns.add(new LayoutColumn("CLLTN_AMT_1_SIGN", 1));
        columns.add(new LayoutColumn("CLLTN_AMT_1_VALUE", 11));
        columns.add(new LayoutColumn("CLLTN_AMT_1_DCML", 1));
        columns.add(new LayoutColumn("CLLTN_AMT_2_SIGN", 1));
        columns.add(new LayoutColumn("CLLTN_AMT_2_VALUE", 11));
        columns.add(new LayoutColumn("CLLTN_AMT_2_DCML", 1));
        columns.add(new LayoutColumn("CLLTN_AMT_3_SIGN", 1));
        columns.add(new LayoutColumn("CLLTN_AMT_3_VALUE", 11));
        columns.add(new LayoutColumn("CLLTN_AMT_3_DCML", 1));
        columns.add(new LayoutColumn("CLLTN_AMT_4_SIGN", 1));
        columns.add(new LayoutColumn("CLLTN_AMT_4_VALUE", 11));
        columns.add(new LayoutColumn("CLLTN_AMT_4_DCML", 1));
        columns.add(new LayoutColumn("CLLTN_CCY_UPR_LWR_LMT", 3));
        columns.add(new LayoutColumn("CLLTN_UPR_LMT_SIGN", 1));
        columns.add(new LayoutColumn("CLLTN_UPR_LMT_VALUE", 11));
        columns.add(new LayoutColumn("CLLTN_UPR_LMT_DCML", 1));
        columns.add(new LayoutColumn("CLLTN_LWR_LMT_SIGN", 1));
        columns.add(new LayoutColumn("CLLTN_LWR_LMT_VALUE", 11));
        columns.add(new LayoutColumn("CLLTN_LWR_LMT_DCML", 1));
        columns.add(new LayoutColumn("CHRG_COMM_REC_2", 298));
        return Collections.unmodifiableList(columns);
    }

    private String normalizeFileKey(String fileName) {
        String normalized = fileName.trim().toUpperCase(Locale.ROOT);
        int extensionIndex = normalized.lastIndexOf('.');
        if (extensionIndex > 0) {
            return normalized.substring(0, extensionIndex);
        }
        return normalized;
    }

    private static class LayoutColumn {

        private final String name;
        private final int length;

        private LayoutColumn(String name, int length) {
            this.name = name;
            this.length = length;
        }

        private String getName() {
            return name;
        }

        private int getLength() {
            return length;
        }
    }
}
