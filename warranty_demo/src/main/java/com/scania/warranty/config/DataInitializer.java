package com.scania.warranty.config;

import com.scania.warranty.domain.Claim;
import com.scania.warranty.domain.ClaimError;
import com.scania.warranty.domain.ClaimStatus;
import com.scania.warranty.domain.Invoice;
import com.scania.warranty.repository.ClaimErrorRepository;
import com.scania.warranty.repository.ClaimRepository;
import com.scania.warranty.repository.InvoiceRepository;
import com.scania.warranty.service.SeedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;

/**
 * Seeds demo Claims and Invoices so that:
 * - POST /api/claims/search?companyCode=001 returns at least one row for the HS1210D UI
 * - POST /api/claims/create works for demo invoices (88888, 77777, 12345, 99999)
 */
@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private static final String SEED_COMPANY = "001";
    private static final String SEED_INVOICE_NUMBER = "12345";
    private static final String SEED_INVOICE_DATE = "20240115";
    private static final String SEED_JOB_NUMBER = "001";
    private static final String SEED_WORKSHOP_TYPE = "A";
    private static final String SEED_AREA = "1";
    private static final String SEED_SPLIT = "04";

    private static final String SEED_FEHLER_NR = "01";
    private static final String SEED_FOLGE_NR = "01";

    private final ClaimRepository claimRepository;
    private final ClaimErrorRepository claimErrorRepository;
    private final InvoiceRepository invoiceRepository;
    private final SeedService seedService;
    private final Environment environment;

    public DataInitializer(ClaimRepository claimRepository,
                           ClaimErrorRepository claimErrorRepository,
                           InvoiceRepository invoiceRepository,
                           SeedService seedService,
                           Environment environment) {
        this.claimRepository = claimRepository;
        this.claimErrorRepository = claimErrorRepository;
        this.invoiceRepository = invoiceRepository;
        this.seedService = seedService;
        this.environment = environment;
    }

    private boolean isTestProfile() {
        return Arrays.stream(environment.getActiveProfiles()).anyMatch("test"::equals);
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            seedData();
        } catch (Exception e) {
            log.error("DataInitializer: Could not seed demo data: {}", e.getMessage(), e);
            // Do not rethrow on startup - app should still run; use POST /api/seed to retry
        }
    }

    /** Called on startup and optionally via POST /api/seed */
    public void seedData() {
        doSeed();
        long count = claimRepository.count();
        log.info("DataInitializer: Seed complete. Total claims in DB: {}", count);
    }

    private void doSeed() {
        if (!isTestProfile()) {
            // Seed invoices first via SeedService (@Transactional ensures commit)
            try {
                int created = seedService.seedInvoices();
                log.info("DataInitializer: Seeded {} invoices for company {}", created, SEED_COMPANY);
            } catch (Exception e) {
                log.error("DataInitializer: Invoice seeding failed: {}", e.getMessage(), e);
                // If invoices fail, try legacy path for backward compatibility
                try {
                    seedInvoiceIfMissing(SEED_COMPANY, "12345", SEED_INVOICE_DATE, "001", SEED_AREA);
                    seedInvoiceIfMissing(SEED_COMPANY, "99999", SEED_INVOICE_DATE, "002", SEED_AREA);
                    seedInvoiceIfMissing(SEED_COMPANY, "88888", SEED_INVOICE_DATE, "003", SEED_AREA);
                    seedInvoiceIfMissing(SEED_COMPANY, "77777", SEED_INVOICE_DATE, "004", SEED_AREA);
                    log.info("DataInitializer: Seeded invoices via legacy path");
                } catch (Exception e2) {
                    log.warn("DataInitializer: Invoice seeding skipped: {}. Delete ./data/warranty_db* and restart for fresh schema.", e2.getMessage());
                }
            }
            // Seed dummy claims so claims list shows data on first load (HSG71PF)
            seedClaimIfMissing(SEED_COMPANY, SEED_INVOICE_NUMBER, SEED_INVOICE_DATE, SEED_JOB_NUMBER, "00000001", "Demo Customer", "100001", "YSA1234", ClaimStatus.PENDING.getCode());
            seedClaimIfMissing(SEED_COMPANY, "99999", SEED_INVOICE_DATE, "002", "00000002", "Demo Customer 2", "100002", "YSA6789", ClaimStatus.PENDING.getCode());
            seedClaimIfMissing(SEED_COMPANY, "88888", SEED_INVOICE_DATE, "003", "00000003", "Demo Customer 88888", "100088", "YSA8888", ClaimStatus.PENDING.getCode());
            seedClaimIfMissing(SEED_COMPANY, "77777", SEED_INVOICE_DATE, "004", "00000004", "Demo Customer 77777", "100077", "YSA7777", ClaimStatus.PENDING.getCode());
            seedClaimIfMissing(SEED_COMPANY, "55555", SEED_INVOICE_DATE, "005", "00000005", "Demo Customer 55555", "100055", "YSA5555", ClaimStatus.APPROVED.getCode());
            log.info("DataInitializer: Seeded claims for company {} (invoices 12345, 99999, 88888, 77777, 55555)", SEED_COMPANY);
        }
    }

    private void seedInvoiceIfMissing(String pakz, String rnr, String rdat, String anr, String berei) {
        if (invoiceRepository.findByKey(pakz, rnr, rdat, anr).isPresent()) {
            return;
        }
        Invoice inv = createMinimalInvoice(pakz, rnr, rdat, anr, berei);
        invoiceRepository.save(inv);
    }

    private Invoice createMinimalInvoice(String pakz, String rnr, String rdat, String anr, String berei) {
        Invoice inv = new Invoice();
        inv.setAhk000(pakz);
        inv.setAhk010(rnr);
        inv.setAhk015(rnr.length() <= 10 ? rnr : rnr.substring(0, 10));
        inv.setAhk020(rdat);
        inv.setAhk030(" ");
        inv.setAhk040(anr);
        inv.setAhk050(SEED_WORKSHOP_TYPE);
        inv.setAhk060(berei);
        inv.setAhk070(SEED_SPLIT);
        inv.setAhk080(rdat);
        inv.setAhk090("Demo invoice " + rnr);
        inv.setAhk100("00000");
        inv.setAhk105("00000");
        inv.setAhk106("20240115");
        inv.setAhk107("00000");
        inv.setAhk108("20240115");
        inv.setAhk110(" ");
        inv.setAhk120(" ");
        inv.setAhk130(BigDecimal.ZERO);
        inv.setAhk140(BigDecimal.ZERO);
        inv.setAhk150("  ");
        inv.setAhk160("00000");
        inv.setAhk170("00000");
        inv.setAhk180("000000");
        inv.setAhk190("000000");
        inv.setAhk200("000000");
        inv.setAhk205("000000");
        inv.setAhk210("1234567");  // chassis - 7 chars for claim g71060
        inv.setAhk220("00000");
        inv.setAhk221("          ");
        inv.setAhk222("001");
        inv.setAhk223("100001");
        inv.setAhk224("100001");
        inv.setAhk225(" ");
        inv.setAhk226(" ");
        inv.setAhk230("100001");   // customer nr
        inv.setAhk240(" ");
        inv.setAhk250("Demo Customer " + rnr);  // customer name
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
        inv.setAhk510("1234567");  // chassis 7 chars - required for claim creation
        inv.setAhk520("20240115"); // repair date
        inv.setAhk530("000000");
        inv.setAhk540("   ");
        inv.setAhk550("00000000"); // required for claim
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
        inv.setAhk810("20240115");
        inv.setAhk815(BigDecimal.valueOf(45000));  // mileage
        inv.setAhk820(BigDecimal.ZERO);
        inv.setAhk830(BigDecimal.ZERO);
        inv.setAhk840(BigDecimal.ZERO);
        inv.setAhk845(BigDecimal.ZERO);
        inv.setAhk850(BigDecimal.ZERO);
        inv.setAhk855("0000000000");
        inv.setAhk860("  ");
        inv.setAhk870(" ");
        inv.setAhk880(" ");
        inv.setAhk890(BigDecimal.ZERO);
        inv.setAhk900("2024");
        inv.setAhk901(" ");
        inv.setAhk910(" ");
        inv.setAhk920(BigDecimal.ZERO);
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

    private void seedClaimIfMissing(String pakz, String rechNr, String rechDatum, String auftragsNr, String claimNr, String kdName, String kdNr, String chassis, int statusCode) {
        if (!claimRepository.findByInvoiceKeyPartial(pakz, rechNr, rechDatum, auftragsNr).isEmpty()) {
            return;
        }
        Claim claim = new Claim();
        claim.setG71000(pakz);
        claim.setG71010(rechNr);
        claim.setG71020(rechDatum);
        claim.setG71030(auftragsNr);
        claim.setG71040(SEED_AREA);
        claim.setG71050(claimNr);
        claim.setG71060(chassis);  // chassis 7 chars - part of ClaimId
        String regNr = "R" + claimNr;
        claim.setG71070(regNr.substring(0, Math.min(10, regNr.length())));  // G71070 max 10 chars
        claim.setG71080(BigDecimal.ZERO);
        claim.setG71090(new BigDecimal(rechDatum));
        claim.setG71100(BigDecimal.valueOf(45));  // G71100 NUMERIC(3,0) max 999
        claim.setG71110(BigDecimal.ONE);
        claim.setG71120(" ");
        claim.setG71130(" ");
        claim.setG71140(kdNr);
        claim.setG71150(kdName);
        claim.setG71160("");
        claim.setG71170(statusCode);
        claim.setG71180(0);
        claim.setG71190(" ");
        claim.setG71200(auftragsNr + SEED_WORKSHOP_TYPE + SEED_AREA + SEED_SPLIT);
        claimRepository.save(claim);

        // ClaimError seeding for first two claims so "Errors" column shows data
        try {
            if ("00000001".equals(claimNr) || "00000003".equals(claimNr)) {
                seedClaimError(pakz, rechNr, rechDatum, auftragsNr, claimNr);
            }
        } catch (Exception e) {
            log.warn("DataInitializer: Skipped ClaimError for claim {}: {}", claimNr, e.getMessage());
        }
    }

    private void seedClaimError(String pakz, String rechNr, String rechDatum, String auftragsNr, String claimNr) {
        ClaimError err = new ClaimError();
        err.setG73000(pakz);
        err.setG73010(rechNr);
        err.setG73020(rechDatum);
        err.setG73030(auftragsNr);
        err.setG73040(SEED_AREA);
        err.setG73050(claimNr);
        err.setG73060(SEED_FEHLER_NR);
        err.setG73065(SEED_FOLGE_NR);
        err.setG73070("");
        err.setG73080("01");
        err.setG73090("01");
        err.setG73100("");
        err.setG73110("");
        err.setG73120("Demo failure description");
        err.setG73130("");
        err.setG73140("");
        err.setG73150("");
        err.setG73160(BigDecimal.ZERO);
        err.setG73170(BigDecimal.ZERO);
        err.setG73180(BigDecimal.ZERO);
        err.setG73190(BigDecimal.ZERO);
        err.setG73200(BigDecimal.ZERO);
        err.setG73210(BigDecimal.ZERO);
        err.setG73220(BigDecimal.ZERO);
        err.setG73230(BigDecimal.ZERO);
        err.setG73240(BigDecimal.ZERO);
        err.setG73250(BigDecimal.ZERO);
        err.setG73260(BigDecimal.ZERO);
        err.setG73270(BigDecimal.ZERO);
        err.setG73280("00000000");
        err.setG73285("                    ");
        err.setG73290(0);
        err.setG73300(BigDecimal.ZERO);
        err.setG73310(BigDecimal.ZERO);
        err.setG73320("");
        err.setG73330("");
        err.setG73340("");
        err.setG73350(" ");
        err.setG73360("00000");
        err.setG73370("00000");
        err.setG73380("00000");
        err.setG73390("00000");
        err.setG73400("00000");
        err.setG73410("00000");
        err.setG73420("");
        err.setG73430("");
        err.setG73440("00000");
        err.setG73450("");
        err.setG73460("00000");
        err.setG73470("");
        err.setG73480("00000");
        err.setG73490("");
        err.setG73500("00000");
        claimErrorRepository.save(err);
    }
}