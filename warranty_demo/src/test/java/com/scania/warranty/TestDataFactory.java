package com.scania.warranty;

import com.scania.warranty.domain.Invoice;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Factory for test seed data. Used by integration tests that need Invoice, etc.
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
    public static final String SEED_WETE = "1";
    public static final String SEED_BEREI = "1";
    /** Must be "04" to match ClaimCreationService.findByInvoiceKey (warranty invoices) */
    public static final String SEED_SPLITT = "04";

    /**
     * Creates a minimal Invoice for claim creation tests.
     * adat is set to a recent date (within 365 days) so repair date check passes.
     */
    public Invoice createSeedInvoice() {
        Invoice inv = new Invoice();
        inv.setPakz(SEED_PAKZ);
        inv.setRnr(SEED_RNR);
        inv.setRdat(SEED_RDAT);
        inv.setAdat(SEED_ADAT);
        inv.setAnr(SEED_ANR);
        inv.setBerei(SEED_BEREI);
        inv.setWt(SEED_WETE);
        inv.setSplitt(SEED_SPLITT);
        inv.setKzS(" ");
        inv.setFahrgnr("YS2R4X20005301234");
        inv.setKz("ABC123");
        inv.setZdat("20150101");
        inv.setKm("50000");
        inv.setKundenNr("12345");
        inv.setName("Test Customer");
        inv.setAnTag(SEED_ADAT);
        inv.setAtext("Test invoice for claim creation");

        // Required fields - use minimal defaults
        setDefaults(inv);
        return inv;
    }

    private void setDefaults(Invoice inv) {
        inv.setRgNr10a("0000000001");
        inv.setLrnr("00001");
        inv.setStoBezRe("00001");
        inv.setStoBezRedat("20240101");
        inv.setKorBezRe("00001");
        inv.setKorBezRedat("20240101");
        inv.setBfort(" ");
        inv.setMwstYn("N");
        inv.setMwstPercent(BigDecimal.ZERO);
        inv.setMwstPercentR(BigDecimal.ZERO);
        inv.setBaSchluessel("  ");
        inv.setKstLohn("00001");
        inv.setKstTeile("00001");
        inv.setFibuMwst("000000");
        inv.setFibuMwstAt("000000");
        inv.setFibuInterim("000000");
        inv.setKtoIntauf("000000");
        inv.setKtrIntAuf("0000000");
        inv.setKstIntAuf("00001");
        inv.setSpezCode("          ");
        inv.setBranch("001");
        inv.setProdCode("          ");
        inv.setProjekt("          ");
        inv.setDokumentennummer("                    ");
        inv.setKostencodeKonzint("   ");
        inv.setAnrede(" ");
        inv.setBranche("                         ");
        inv.setMatch("00001");
        inv.setStrasse("                         ");
        inv.setLand("   ");
        inv.setPlz("00000");
        inv.setOrt("                    ");
        inv.setTelefon("                 ");
        inv.setBestellerKunde("                    ");
        inv.setValuta(" ");
        inv.setBonitaet(" ");
        inv.setZahlungsart(" ");
        inv.setRc("   ");
        inv.setReKundenNr("000000");
        inv.setReAnrede(" ");
        inv.setReName("                              ");
        inv.setReBranche("                         ");
        inv.setReMatch("00001");
        inv.setReStrasse("                         ");
        inv.setReland("   ");
        inv.setRePlz("00000");
        inv.setReOrt("                    ");
        inv.setReTele("                 ");
        inv.setReValuta(" ");
        inv.setReBonitaet(" ");
        inv.setReZart(" ");
        inv.setReRc("   ");
        inv.setUstIdNrOk("                    ");
        inv.setTyp("               ");
        inv.setBj("2024");
        inv.setWrg("   ");
        inv.setAu("000000");
        inv.setGa("20240101");
        inv.setSp("000000");
        inv.setTacho("00000000");
        inv.setHu("000000");
        inv.setAnZeit("0000");
        inv.setFertTag("20240115");
        inv.setFertZeit("0000");
        inv.setBerater("                    ");
        inv.setLeitzahl("000");
        inv.setTxAnf("000");
        inv.setTxEnde("000");
        inv.setMotornr("          ");
        inv.setMotorTyp("                    ");
        inv.setUserAuftrag("          ");
        inv.setUserRechnung("          ");
        inv.setRgsNetto(BigDecimal.ZERO);
        inv.setRgsBasisAt(BigDecimal.ZERO);
        inv.setRgsBasisMwst(BigDecimal.ZERO);
        inv.setRgsMwst(BigDecimal.ZERO);
        inv.setRgsMwstAt(BigDecimal.ZERO);
        inv.setRgsGesBrutto(BigDecimal.ZERO);
        inv.setEgUmsatz(" ");
        inv.setSteuerfreiDrittland(" ");
        inv.setVerbucht(" ");
        inv.setReserve1(BigDecimal.ZERO);
        inv.setReserve2(BigDecimal.ZERO);
        inv.setGaUebern("00000000");
        inv.setWktId(0);
        inv.setReserve3(0);
        inv.setReserve4(0);
        inv.setFvGt0(0);
        inv.setFbGt0(0);
        inv.setKampagneNr(0);
        inv.setSpoOrder("          ");
        inv.setKenAv("  ");
        inv.setKenPe("  ");
        inv.setKlrBerech(" ");
        inv.setKlrBetrag(BigDecimal.ZERO);
        inv.setAssiVorgangNr("               ");
        inv.setZagaGueltig("00000000");
        inv.setRwFreigabeNr("               ");
        inv.setKlErweiterung(0);
        inv.setKlAusnahmeIdnr("   ");
        inv.setKlAusnahmeKlartext("                                        ");
        inv.setFahrzeugArt("                    ");
        inv.setHersteller("                    ");
        inv.setAufbauart("                    ");
        inv.setHerstellerAufbau("                    ");
        inv.setZusatzausruestung1("                    ");
        inv.setHerstellerZusatz1("                    ");
        inv.setZusatzausruestung2("                    ");
        inv.setHerstellerZusatz2("                    ");
        inv.setZusatzausruestung3("                    ");
        inv.setHerstellerZusatz3("                    ");
        inv.setEinsatzart("                    ");
        inv.setEuroNorm("          ");
        inv.setPartikelfilter(" ");
        inv.setIsArt("     ");
        inv.setMailTo("");
        inv.setMailCc("");
    }
}
