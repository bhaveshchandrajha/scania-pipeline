package com.scania.warranty.service;

import com.scania.warranty.domain.Invoice;
import com.scania.warranty.repository.InvoiceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seeds demo invoices into HSAHKLF3. Uses @Transactional to ensure commits.
 * Call via POST /api/seed or /api/seed-invoices.
 */
@Service
public class SeedService {

    private static final Logger log = LoggerFactory.getLogger(SeedService.class);

    private static final String SEED_COMPANY = "001";
    private static final String SEED_DATE = "20240115";
    private static final String SEED_WORKSHOP = "A";
    private static final String SEED_AREA = "1";
    private static final String SEED_SPLIT = "04";

    private final InvoiceRepository invoiceRepository;

    public SeedService(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    @Transactional
    public int seedInvoices() {
        int created = 0;
        // invoiceNr, orderNr, customerName, customerNr, chassis (7 chars max for G71060/ahk510)
        String[][] rows = {
            {"12345", "001", "Demo Customer", "100001", "YSA1234"},
            {"99999", "002", "Demo Customer 2", "100002", "YSA6789"},
            {"88888", "003", "Demo Customer 88888", "100088", "YSA8888"},
            {"77777", "004", "Demo Customer 77777", "100077", "YSA7777"},
            {"55555", "005", "Demo Customer 55555", "100055", "YSA5555"},
            {"44444", "006", "Demo Customer 44444", "100044", "YSA4444"},
            {"33333", "007", "Demo Customer 33333", "100033", "YSA3333"},
        };
        for (String[] row : rows) {
            try {
                created += seedOne(row[0], row[1], row[2], row[3], row[4]);
            } catch (Exception e) {
                log.error("SeedService: Failed to seed invoice {}: {}", row[0], e.getMessage(), e);
                throw new RuntimeException("Failed to seed invoice " + row[0] + ": " + e.getMessage(), e);
            }
        }
        log.info("SeedService: Created {} invoices", created);
        return created;
    }

    private int seedOne(String invoiceNr, String orderNr, String customerName, String customerNr, String chassis) {
        if (invoiceRepository.findByKey(SEED_COMPANY, invoiceNr, SEED_DATE, orderNr).isPresent()) {
            return 0;
        }
        Invoice inv = createMinimalInvoice(invoiceNr, orderNr, customerName, customerNr, chassis);
        invoiceRepository.save(inv);
        return 1;
    }

    private Invoice createMinimalInvoice(String rnr, String anr, String customerName, String customerNr, String chassis) {
        Invoice inv = new Invoice();
        inv.setAhk000(SEED_COMPANY);
        inv.setAhk010(rnr);
        inv.setAhk015(rnr.length() <= 10 ? rnr : rnr.substring(0, 10));
        inv.setAhk020(SEED_DATE);
        inv.setAhk030(" ");
        inv.setAhk040(anr);
        inv.setAhk050(SEED_WORKSHOP);
        inv.setAhk060(SEED_AREA);
        inv.setAhk070(SEED_SPLIT);
        inv.setAhk080(SEED_DATE);
        inv.setAhk090("Demo invoice " + rnr);
        inv.setAhk100("00000");
        inv.setAhk105("00000");
        inv.setAhk106("20240115");
        inv.setAhk107("00000");
        inv.setAhk108("20240115");
        inv.setAhk110(" ");
        inv.setAhk120(" ");
        inv.setAhk130(java.math.BigDecimal.ZERO);
        inv.setAhk140(java.math.BigDecimal.ZERO);
        inv.setAhk150("  ");
        inv.setAhk160("00000");
        inv.setAhk170("00000");
        inv.setAhk180("000000");
        inv.setAhk190("000000");
        inv.setAhk200("000000");
        inv.setAhk205("000000");
        inv.setAhk210(chassis != null ? chassis : "1234567");
        inv.setAhk220("00000");
        inv.setAhk221("          ");
        inv.setAhk222("001");
        inv.setAhk223(customerNr != null ? customerNr : "100001");
        inv.setAhk224(customerNr != null ? customerNr : "100001");
        inv.setAhk225(" ");
        inv.setAhk226(" ");
        inv.setAhk230(customerNr != null ? customerNr : "100001");
        inv.setAhk240(" ");
        inv.setAhk250(customerName != null ? customerName : "Demo Customer " + rnr);
        inv.setAhk260(" ");
        inv.setAhk270(" ");
        inv.setAhk280(" ");
        inv.setAhk290(" ");
        inv.setAhk300(" ");
        inv.setAhk310(" ");
        inv.setAhk320(" ");
        inv.setAhk325(" ");
        inv.setAhk330(" ");
        inv.setAhk340(" ");
        inv.setAhk350(" ");
        inv.setAhk360(" ");
        inv.setAhk370(" ");
        inv.setAhk380(" ");
        inv.setAhk390(" ");
        inv.setAhk400(" ");
        inv.setAhk410(" ");
        inv.setAhk420(" ");
        inv.setAhk430(" ");
        inv.setAhk440(" ");
        inv.setAhk450(" ");
        inv.setAhk460(" ");
        inv.setAhk470(" ");
        inv.setAhk480(" ");
        inv.setAhk490(" ");
        inv.setAhk500("001");
        inv.setAhk505("INV" + rnr);
        inv.setAhk510(chassis != null ? chassis : "1234567");
        inv.setAhk520("20240115");
        inv.setAhk530("000000");
        inv.setAhk540("   ");
        inv.setAhk550("00000000");
        inv.setAhk560("EUR");
        inv.setAhk570("000000");
        inv.setAhk580("20240115");
        inv.setAhk590("000000");
        inv.setAhk595("20240115");
        inv.setAhk600("20240115");
        inv.setAhk610("000000");
        inv.setAhk620("20240115");
        inv.setAhk625("0000");
        inv.setAhk630("20240115");
        inv.setAhk640("0000");
        inv.setAhk650("Demo");
        inv.setAhk660("DEU");
        inv.setAhk670("001");
        inv.setAhk680("   ");
        inv.setAhk690("0000000000");
        inv.setAhk691("                    ");
        inv.setAhk699("0000000000");
        inv.setAhk700("0000000000");
        inv.setAhk710(java.math.BigDecimal.ZERO);
        inv.setAhk720(java.math.BigDecimal.ZERO);
        inv.setAhk730(java.math.BigDecimal.ZERO);
        inv.setAhk740(java.math.BigDecimal.ZERO);
        inv.setAhk750(java.math.BigDecimal.ZERO);
        inv.setAhk760(java.math.BigDecimal.ZERO);
        inv.setAhk770(" ");
        inv.setAhk775(" ");
        inv.setAhk780(" ");
        inv.setAhk790(java.math.BigDecimal.ZERO);
        inv.setAhk800(java.math.BigDecimal.ZERO);
        inv.setAhk810("20240115");
        inv.setAhk815(java.math.BigDecimal.valueOf(45000));
        inv.setAhk820(java.math.BigDecimal.ZERO);
        inv.setAhk830(java.math.BigDecimal.ZERO);
        inv.setAhk840(java.math.BigDecimal.ZERO);
        inv.setAhk845(java.math.BigDecimal.ZERO);
        inv.setAhk850(java.math.BigDecimal.ZERO);
        inv.setAhk855("0000000000");
        inv.setAhk860("  ");
        inv.setAhk870(" ");
        inv.setAhk880(" ");
        inv.setAhk890(java.math.BigDecimal.ZERO);
        inv.setAhk900("2024");
        inv.setAhk901(" ");
        inv.setAhk910(" ");
        inv.setAhk920(java.math.BigDecimal.ZERO);
        inv.setAhk930(" ");
        inv.setAhk940(" ");
        inv.setAhk950(" ");
        inv.setAhk960(" ");
        inv.setAhk970(" ");
        inv.setAhk980(" ");
        inv.setAhk990(" ");
        inv.setAhk1000(" ");
        inv.setAhk1010(" ");
        inv.setAhk1020(" ");
        inv.setAhk1030(" ");
        inv.setAhk1040(" ");
        inv.setAhk1050(" ");
        inv.setAhk1060(" ");
        inv.setAhk1070(" ");
        inv.setAhk1080("00000");
        inv.setAhk1100("");
        inv.setAhk1110("");
        return inv;
    }
}