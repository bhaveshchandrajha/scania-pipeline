package com.scania.warranty.config;

import com.scania.warranty.domain.Claim;
import com.scania.warranty.domain.ClaimError;
import com.scania.warranty.domain.ClaimStatus;
import com.scania.warranty.domain.Invoice;
import com.scania.warranty.repository.ClaimErrorRepository;
import com.scania.warranty.repository.ClaimRepository;
import com.scania.warranty.repository.InvoiceRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Seeds one demo Invoice and one Claim so that:
 * - POST /api/claims with companyCode=001, invoiceNumber=12345, invoiceDate=20240115 succeeds
 * - GET /api/claims/search?companyCode=001 returns at least one row for the HS1210D UI
 */
@Component
public class DataInitializer implements ApplicationRunner {

    private static final String SEED_COMPANY = "001";
    private static final String SEED_INVOICE_NUMBER = "12345";
    private static final String SEED_INVOICE_DATE = "20240115";
    private static final String SEED_JOB_NUMBER = "001";
    private static final String SEED_WORKSHOP_TYPE = "A";
    private static final String SEED_AREA = "1";
    private static final String SEED_SPLIT = "04";

    private static final String SEED_FEHLER_NR = "01";
    private static final String SEED_FOLGE_NR = "01";

    private final InvoiceRepository invoiceRepository;
    private final ClaimRepository claimRepository;
    private final ClaimErrorRepository claimErrorRepository;
    private final Environment environment;

    public DataInitializer(InvoiceRepository invoiceRepository, ClaimRepository claimRepository,
                           ClaimErrorRepository claimErrorRepository, Environment environment) {
        this.invoiceRepository = invoiceRepository;
        this.claimRepository = claimRepository;
        this.claimErrorRepository = claimErrorRepository;
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
            // Demo data is optional; app can run without it
            System.err.println("DataInitializer: Could not seed demo data: " + e.getMessage());
        }
    }

    private void seedData() {
        if (invoiceRepository.findByInvoiceKey(
                SEED_COMPANY, SEED_INVOICE_NUMBER, SEED_INVOICE_DATE, SEED_JOB_NUMBER, SEED_AREA, SEED_WORKSHOP_TYPE, SEED_SPLIT).isEmpty()) {
            Invoice inv = new Invoice();
            inv.setPakz(SEED_COMPANY);
            inv.setRnr(SEED_INVOICE_NUMBER);
            inv.setRdat(SEED_INVOICE_DATE);
            inv.setAnr(SEED_JOB_NUMBER);
            inv.setWt(SEED_WORKSHOP_TYPE);
            inv.setBerei(SEED_AREA);
            inv.setSplitt(SEED_SPLIT);
            inv.setAdat(SEED_INVOICE_DATE);
            inv.setAtext("");
            inv.setFahrgnr("00000001234567");
            inv.setKz("ABC123");
            inv.setZdat("20230101");
            inv.setName("Demo Customer");
            inv.setKundenNr("100001");
            inv.setKm("45000");
            inv.setAnTag(SEED_INVOICE_DATE);
            inv.setAnZeit("0000");
            inv.setFertTag(SEED_INVOICE_DATE);
            inv.setFertZeit("0000");
            inv.setBerater("");
            inv.setLeitzahl("000");
            inv.setTxAnf("000");
            inv.setTxEnde("000");
            inv.setAnrede("");
            inv.setRgNr10a(SEED_INVOICE_NUMBER);
            inv.setLrnr("");
            inv.setStoBezRe("");
            inv.setStoBezRedat("");
            inv.setKorBezRe("");
            inv.setKorBezRedat("");
            inv.setBfort("");
            inv.setMwstYn("");
            inv.setMwstPercent(BigDecimal.ZERO);
            inv.setMwstPercentR(BigDecimal.ZERO);
            inv.setBaSchluessel("");
            inv.setKstLohn("");
            inv.setKstTeile("");
            inv.setFibuMwst("");
            inv.setFibuMwstAt("");
            inv.setFibuInterim("");
            inv.setKtoIntauf("");
            inv.setKtrIntAuf("");
            inv.setKstIntAuf("");
            inv.setSpezCode("");
            inv.setBranch("");
            inv.setProdCode("");
            inv.setProjekt("");
            inv.setDokumentennummer("");
            inv.setKostencodeKonzint("");
            inv.setBranche("");
            inv.setMatch("");
            inv.setStrasse("");
            inv.setLand("");
            inv.setKzS("");
            inv.setAssiVorgangNr("");
            inv.setZagaGueltig("");
            inv.setRwFreigabeNr("");
            inv.setKlErweiterung(0);
            inv.setPlz("12345");
            inv.setOrt("City");
            inv.setTelefon("");
            inv.setBestellerKunde("");
            inv.setValuta("");
            inv.setBonitaet("");
            inv.setZahlungsart("");
            inv.setRc("");
            inv.setReKundenNr("");
            inv.setReAnrede("");
            inv.setReName("");
            inv.setReBranche("");
            inv.setReMatch("");
            inv.setReStrasse("");
            inv.setReland("");
            inv.setRePlz("");
            inv.setReOrt("");
            inv.setReTele("");
            inv.setReValuta("");
            inv.setReBonitaet("");
            inv.setReZart("");
            inv.setReRc("");
            inv.setUstIdNrOk("");
            inv.setTyp("");
            inv.setBj("");
            inv.setWrg("");
            inv.setAu("");
            inv.setGa("");
            inv.setSp("");
            inv.setTacho("");
            inv.setHu("");
            inv.setMotornr("");
            inv.setMotorTyp("");
            inv.setUserAuftrag("");
            inv.setUserRechnung("");
            inv.setRgsNetto(BigDecimal.ZERO);
            inv.setRgsBasisAt(BigDecimal.ZERO);
            inv.setRgsBasisMwst(BigDecimal.ZERO);
            inv.setRgsMwst(BigDecimal.ZERO);
            inv.setRgsMwstAt(BigDecimal.ZERO);
            inv.setRgsGesBrutto(BigDecimal.ZERO);
            inv.setEgUmsatz("");
            inv.setSteuerfreiDrittland("");
            inv.setVerbucht("");
            inv.setReserve1(BigDecimal.ZERO);
            inv.setReserve2(BigDecimal.ZERO);
            inv.setGaUebern("");
            inv.setWktId(0);
            inv.setReserve3(0);
            inv.setReserve4(0);
            inv.setFvGt0(0);
            inv.setFbGt0(0);
            inv.setKampagneNr(0);
            inv.setSpoOrder("");
            inv.setKenAv("");
            inv.setKenPe("");
            inv.setKlrBerech("");
            inv.setKlrBetrag(BigDecimal.ZERO);
            inv.setKlAusnahmeIdnr("");
            inv.setKlAusnahmeKlartext("");
            inv.setFahrzeugArt("");
            inv.setHersteller("");
            inv.setAufbauart("");
            inv.setHerstellerAufbau("");
            inv.setZusatzausruestung1("");
            inv.setHerstellerZusatz1("");
            inv.setZusatzausruestung2("");
            inv.setHerstellerZusatz2("");
            inv.setZusatzausruestung3("");
            inv.setHerstellerZusatz3("");
            inv.setEinsatzart("");
            inv.setEuroNorm("");
            inv.setPartikelfilter("");
            inv.setIsArt("");
            inv.setMailTo("");
            inv.setMailCc("");
            invoiceRepository.save(inv);
        }

        // Second invoice: no claim - for create-during-demo (invoice 99999, order 002)
        seedInvoiceIfMissing(SEED_COMPANY, "99999", "002", "Demo Customer 2", "100002");

        // Third invoice: no claim - for create-demo (invoice 88888, order 003)
        seedInvoiceIfMissing(SEED_COMPANY, "88888", "003", "Demo Customer 3", "100003");
        // Fourth invoice: no claim - for additional create-demo (invoice 77777, order 004)
        seedInvoiceIfMissing(SEED_COMPANY, "77777", "004", "Demo Customer 4", "100004");

        // Seed one claim for first invoice so claims list shows data on first load (HSG71PF).
        // User can create a second claim via form (invoice 99999, order 002) to demonstrate DB write.
        if (!isTestProfile()) {
        List<Claim> existingClaims = claimRepository.findByPakzOrderByClaimNrAsc(SEED_COMPANY);
        if (existingClaims.isEmpty()) {
            Claim claim = new Claim();
            claim.setPakz(SEED_COMPANY);
            claim.setRechNr(SEED_INVOICE_NUMBER);
            claim.setRechDatum(SEED_INVOICE_DATE);
            claim.setAuftragsNr(SEED_JOB_NUMBER);
            claim.setWete(SEED_WORKSHOP_TYPE);
            claim.setClaimNr("00000001");
            claim.setAnhang(" ");
            claim.setAuslaender(" ");
            claim.setChassisNr("1234567");
            claim.setKennzeichen("ABC123");
            claim.setZulDatum(20230101);
            claim.setRepDatum(20240115);
            claim.setKmStand(45);
            claim.setProduktTyp(1);
            claim.setKdNr("100001");
            claim.setKdName("Demo Customer");
            claim.setClaimNrSde("");
            claim.setStatusCodeSde(ClaimStatus.PENDING.getCode());
            claim.setAnzFehler(0);
            claim.setBereich(SEED_AREA);
            claim.setAufNr(SEED_JOB_NUMBER + SEED_WORKSHOP_TYPE + SEED_AREA + SEED_SPLIT);
            claimRepository.save(claim);

            // Seed ClaimError (HSG73PF) - one failure detail for the demo claim
            ClaimError err = new ClaimError();
            err.setPakz(SEED_COMPANY);
            err.setRechNr(SEED_INVOICE_NUMBER);
            err.setRechDatum(SEED_INVOICE_DATE);
            err.setAuftragsNr(SEED_JOB_NUMBER);
            err.setBereich(SEED_AREA);
            err.setClaimNr("00000001");
            err.setFehlerNr(SEED_FEHLER_NR);
            err.setFolgeNr(SEED_FOLGE_NR);
            err.setFehlerTeil("");
            err.setHauptgruppe("01");
            err.setNebengruppe("01");
            err.setSchadC1("");
            err.setSchadC2("");
            err.setText1("Demo failure description");
            err.setText2("");
            err.setSteuerCode("");
            err.setBewCode1("");
            err.setBewCode2(0);
            err.setBewDatum(0);
            err.setVergMat(0);
            err.setVergArb(0);
            err.setVergSpez(0);
            err.setBeantrMat(BigDecimal.ZERO);
            err.setBeantrgArb(BigDecimal.ZERO);
            err.setBeantrgSpez(BigDecimal.ZERO);
            err.setClaimArt(0);
            err.setvRepDatum(0);
            err.setvKmStand(0);
            err.setFeldtestNr(0);
            err.setKampagnenNr("");
            err.setEps("");
            err.setStatusCode(0);
            err.setVariantCode(0);
            err.setActionCode(0);
            err.setText3("");
            err.setText4("");
            err.setFehlerNrSde("");
            err.setAnhang("");
            err.setSource("");
            err.setComplain("");
            err.setSymptom("");
            err.setFailure("");
            err.setLocation("");
            err.setRepair("");
            err.setErgCode("");
            err.setResult1("");
            err.setResult2("");
            err.setFault1("");
            err.setFault2("");
            err.setReply1("");
            err.setReply2("");
            err.setExplanation1("");
            err.setExplanation2("");
            claimErrorRepository.save(err);
        }
        }
    }

    private void seedInvoiceIfMissing(String pakz, String rnr, String anr, String name, String kundenNr) {
        if (invoiceRepository.findByInvoiceKey(pakz, rnr, SEED_INVOICE_DATE, anr, SEED_AREA, SEED_WORKSHOP_TYPE, SEED_SPLIT).isEmpty()) {
            Invoice inv = copyInvoice(pakz, rnr, SEED_INVOICE_DATE, anr);
            inv.setName(name);
            inv.setKundenNr(kundenNr);
            invoiceRepository.save(inv);
        }
    }

    private Invoice copyInvoice(String pakz, String rnr, String rdat, String anr) {
        Invoice inv = new Invoice();
        inv.setPakz(pakz);
        inv.setRnr(rnr);
        inv.setRdat(rdat);
        inv.setAnr(anr);
        inv.setWt(SEED_WORKSHOP_TYPE);
        inv.setBerei(SEED_AREA);
        inv.setSplitt(SEED_SPLIT);
        inv.setAdat(rdat);
        inv.setAtext("");
        inv.setFahrgnr("00000001234567");
        inv.setKz("ABC123");
        inv.setZdat("20230101");
        inv.setName("Demo Customer");
        inv.setKundenNr("100001");
        inv.setKm("45000");
        inv.setAnTag(rdat);
        inv.setAnZeit("0000");
        inv.setFertTag(rdat);
        inv.setFertZeit("0000");
        inv.setBerater("");
        inv.setLeitzahl("000");
        inv.setTxAnf("000");
        inv.setTxEnde("000");
        inv.setAnrede("");
        inv.setRgNr10a(rnr);
        inv.setLrnr("");
        inv.setStoBezRe("");
        inv.setStoBezRedat("");
        inv.setKorBezRe("");
        inv.setKorBezRedat("");
        inv.setBfort("");
        inv.setMwstYn("");
        inv.setMwstPercent(BigDecimal.ZERO);
        inv.setMwstPercentR(BigDecimal.ZERO);
        inv.setBaSchluessel("");
        inv.setKstLohn("");
        inv.setKstTeile("");
        inv.setFibuMwst("");
        inv.setFibuMwstAt("");
        inv.setFibuInterim("");
        inv.setKtoIntauf("");
        inv.setKtrIntAuf("");
        inv.setKstIntAuf("");
        inv.setSpezCode("");
        inv.setBranch("");
        inv.setProdCode("");
        inv.setProjekt("");
        inv.setDokumentennummer("");
        inv.setKostencodeKonzint("");
        inv.setBranche("");
        inv.setMatch("");
        inv.setStrasse("");
        inv.setLand("");
        inv.setKzS("");
        inv.setAssiVorgangNr("");
        inv.setZagaGueltig("");
        inv.setRwFreigabeNr("");
        inv.setKlErweiterung(0);
        inv.setPlz("12345");
        inv.setOrt("City");
        inv.setTelefon("");
        inv.setBestellerKunde("");
        inv.setValuta("");
        inv.setBonitaet("");
        inv.setZahlungsart("");
        inv.setRc("");
        inv.setReKundenNr("");
        inv.setReAnrede("");
        inv.setReName("");
        inv.setReBranche("");
        inv.setReMatch("");
        inv.setReStrasse("");
        inv.setReland("");
        inv.setRePlz("");
        inv.setReOrt("");
        inv.setReTele("");
        inv.setReValuta("");
        inv.setReBonitaet("");
        inv.setReZart("");
        inv.setReRc("");
        inv.setUstIdNrOk("");
        inv.setTyp("");
        inv.setBj("");
        inv.setWrg("");
        inv.setAu("");
        inv.setGa("");
        inv.setSp("");
        inv.setTacho("");
        inv.setHu("");
        inv.setMotornr("");
        inv.setMotorTyp("");
        inv.setUserAuftrag("");
        inv.setUserRechnung("");
        inv.setRgsNetto(BigDecimal.ZERO);
        inv.setRgsBasisAt(BigDecimal.ZERO);
        inv.setRgsBasisMwst(BigDecimal.ZERO);
        inv.setRgsMwst(BigDecimal.ZERO);
        inv.setRgsMwstAt(BigDecimal.ZERO);
        inv.setRgsGesBrutto(BigDecimal.ZERO);
        inv.setEgUmsatz("");
        inv.setSteuerfreiDrittland("");
        inv.setVerbucht("");
        inv.setReserve1(BigDecimal.ZERO);
        inv.setReserve2(BigDecimal.ZERO);
        inv.setGaUebern("");
        inv.setWktId(0);
        inv.setReserve3(0);
        inv.setReserve4(0);
        inv.setFvGt0(0);
        inv.setFbGt0(0);
        inv.setKampagneNr(0);
        inv.setSpoOrder("");
        inv.setKenAv("");
        inv.setKenPe("");
        inv.setKlrBerech("");
        inv.setKlrBetrag(BigDecimal.ZERO);
        inv.setKlAusnahmeIdnr("");
        inv.setKlAusnahmeKlartext("");
        inv.setFahrzeugArt("");
        inv.setHersteller("");
        inv.setAufbauart("");
        inv.setHerstellerAufbau("");
        inv.setZusatzausruestung1("");
        inv.setHerstellerZusatz1("");
        inv.setZusatzausruestung2("");
        inv.setHerstellerZusatz2("");
        inv.setZusatzausruestung3("");
        inv.setHerstellerZusatz3("");
        inv.setEinsatzart("");
        inv.setEuroNorm("");
        inv.setPartikelfilter("");
        inv.setIsArt("");
        inv.setMailTo("");
        inv.setMailCc("");
        return inv;
    }
}