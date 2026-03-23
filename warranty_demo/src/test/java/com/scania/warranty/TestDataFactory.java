package com.scania.warranty;

import com.scania.warranty.domain.Invoice;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Factory for test seed data. Used by integration tests that need Invoice, etc.
 * Uses Invoice entity AHK column names (setAhkXXX) - no logical aliases.
 */
@Component
public class TestDataFactory {

    public static final String SEED_PAKZ = "001";
    public static final String SEED_RNR = "12345";
    /** Invoice date - must match test's SEED_DATE for lookup */
    public static final String SEED_RDAT = "20240115";
    /** Repair date - must be within 365 days for claim creation */
    public static final String SEED_ADAT = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    public static final String SEED_ANR = "001";
    /** Must be "A" to match warranty type */
    public static final String SEED_WT = "A";
    /** Must be "1" to match ClaimCreationWorkflowTest SEED_WORKSHOP */
    public static final String SEED_WORKSHOP = "1";
    public static final String SEED_BEREI = "1";
    /** Must be "04" to match warranty invoices */
    public static final String SEED_SPLITT = "04";

    /**
     * Creates a minimal Invoice for claim creation tests.
     * adat is set to a recent date (within 365 days) so repair date check passes.
     */
    public Invoice createSeedInvoice() {
        Invoice inv = new Invoice();
        inv.setAhk000(SEED_PAKZ);
        inv.setAhk010(SEED_RNR);
        inv.setAhk015(SEED_RNR.length() <= 10 ? SEED_RNR : SEED_RNR.substring(0, 10));
        inv.setAhk020(SEED_RDAT);
        inv.setAhk030(" ");
        inv.setAhk040(SEED_ANR);
        inv.setAhk050(SEED_WORKSHOP);
        inv.setAhk060(SEED_BEREI);
        inv.setAhk070(SEED_SPLITT);
        inv.setAhk080(SEED_RDAT);  // Must match SEED_DATE for ClaimCreationService lookup
        inv.setAhk090("Test invoice for claim creation");
        inv.setAhk510("YS2R4X2");  // max 17 for AHK510, but Claim.g71060 is length 7
        inv.setAhk520("ABC123");
        inv.setAhk550("00000001");
        inv.setAhk230("100001");
        inv.setAhk250("Test Customer");

        setDefaults(inv);
        return inv;
    }

    private void setDefaults(Invoice inv) {
        inv.setAhk100("00001");
        inv.setAhk105("00001");
        inv.setAhk106("20240101");
        inv.setAhk107("00001");
        inv.setAhk108("20240101");
        inv.setAhk110(" ");
        inv.setAhk120(" ");
        inv.setAhk130(BigDecimal.ZERO);
        inv.setAhk140(BigDecimal.ZERO);
        inv.setAhk150("  ");
        inv.setAhk160("00001");
        inv.setAhk170("00001");
        inv.setAhk180("000000");
        inv.setAhk190("000000");
        inv.setAhk200("000000");
        inv.setAhk205("000000");
        inv.setAhk210("0000001");
        inv.setAhk220("00001");
        inv.setAhk221("0000000001");
        inv.setAhk222("   ");
        inv.setAhk223("20240101");
        inv.setAhk224("20240101");
        inv.setAhk225("00001");
        inv.setAhk226("   ");
        inv.setAhk240(" ");
        inv.setAhk260("                         ");
        inv.setAhk270("00001");
        inv.setAhk280("                         ");
        inv.setAhk290("   ");
        inv.setAhk300("00001");
        inv.setAhk310("                    ");
        inv.setAhk320("                 ");
        inv.setAhk325("                    ");
        inv.setAhk330(" ");
        inv.setAhk340(" ");
        inv.setAhk350(" ");
        inv.setAhk360("   ");
        inv.setAhk370("000000");
        inv.setAhk380(" ");
        inv.setAhk390("                    ");
        inv.setAhk400("                         ");
        inv.setAhk410("00001");
        inv.setAhk420("                         ");
        inv.setAhk430("   ");
        inv.setAhk440("00001");
        inv.setAhk450("                    ");
        inv.setAhk460("                 ");
        inv.setAhk470(" ");
        inv.setAhk480(" ");
        inv.setAhk490(" ");
        inv.setAhk500("   ");
        inv.setAhk505("YS2R4X20005301234   ");
        inv.setAhk530("            ");
        inv.setAhk540("0000");
        inv.setAhk560("   ");
        inv.setAhk570("000000");
        inv.setAhk580("00000000");
        inv.setAhk590("000000");
        inv.setAhk595("00000000");
        inv.setAhk600("00000000");
        inv.setAhk610("000000");
        inv.setAhk620("20150101");
        inv.setAhk625("0000");
        inv.setAhk630("00000000");
        inv.setAhk640("0000");
        inv.setAhk650("                    ");
        inv.setAhk660("   ");
        inv.setAhk670("   ");
        inv.setAhk680("   ");
        inv.setAhk690("          ");
        inv.setAhk691("                    ");
        inv.setAhk699("          ");
        inv.setAhk700("          ");
        inv.setAhk710(BigDecimal.ZERO);
        inv.setAhk720(BigDecimal.ZERO);
        inv.setAhk730(BigDecimal.ZERO);
        inv.setAhk740(BigDecimal.ZERO);
        inv.setAhk750(BigDecimal.ZERO);
        inv.setAhk760(BigDecimal.ZERO);
        inv.setAhk770(" ");
        inv.setAhk775(" ");
        inv.setAhk780(" ");
        inv.setAhk790(BigDecimal.ZERO);
        inv.setAhk800(BigDecimal.ZERO);
        inv.setAhk810("00000000");
        inv.setAhk815(BigDecimal.ZERO);
        inv.setAhk820(BigDecimal.ZERO);
        inv.setAhk830(BigDecimal.ZERO);
        inv.setAhk840(BigDecimal.ZERO);
        inv.setAhk845(BigDecimal.ZERO);
        inv.setAhk850(BigDecimal.ZERO);
        inv.setAhk855("          ");
        inv.setAhk860("  ");
        inv.setAhk870("  ");
        inv.setAhk880(" ");
        inv.setAhk890(BigDecimal.ZERO);
        inv.setAhk900("               ");
        inv.setAhk901("00000000");
        inv.setAhk910("               ");
        inv.setAhk920(BigDecimal.ZERO);
        inv.setAhk930("   ");
        inv.setAhk940("                                        ");
        inv.setAhk950("                    ");
        inv.setAhk960("                    ");
        inv.setAhk970("                    ");
        inv.setAhk980("                    ");
        inv.setAhk990("                    ");
        inv.setAhk1000("                    ");
        inv.setAhk1010("                    ");
        inv.setAhk1020("                    ");
        inv.setAhk1030("                    ");
        inv.setAhk1040("                    ");
        inv.setAhk1050("                    ");
        inv.setAhk1060("          ");
        inv.setAhk1070(" ");
        inv.setAhk1080("00001");
        inv.setAhk1100("");
        inv.setAhk1110("");
    }
}
