package com.scania.warranty.config;

import com.scania.warranty.domain.*;
import com.scania.warranty.repository.*;
import com.scania.warranty.service.ClaimCreationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;

@Configuration
public class DataInitializer {
    
    @Autowired
    private InvoiceRepository invoiceRepository;
    
    @Autowired
    private ClaimRepository claimRepository;
    
    @Autowired
    private ClaimErrorRepository claimErrorRepository;
    
    @Autowired
    private WorkPositionRepository workPositionRepository;
    
    @Autowired
    private WarrantyReleaseRepository warrantyReleaseRepository;
    
    @Autowired
    private HSFLALF1Repository hsflalf1Repository;
    
    @Autowired
    private ClaimCreationService claimCreationService;
    
    @Bean
    @Transactional
    public ApplicationRunner initializeData() {
        return args -> {
            // Only initialize if database is empty
            if (invoiceRepository.count() > 0) {
                System.out.println("Database already contains data, skipping initialization.");
                return;
            }
            
            System.out.println("Initializing sample data...");
            
            // Create sample invoices (matching ClaimCreationService expectations)
            // ClaimCreationService expects: wt="1", splitt="04"
            Invoice invoice1 = createSampleInvoice("001", "12345", "20240115", "001", "A", "1", "04");
            Invoice invoice2 = createSampleInvoice("001", "12346", "20240220", "002", "B", "1", "04");
            Invoice invoice3 = createSampleInvoice("002", "12347", "20240310", "003", "A", "1", "04");
            
            invoiceRepository.saveAll(Arrays.asList(invoice1, invoice2, invoice3));
            System.out.println("Created 3 sample invoices");
            
            // Create sample work positions (required for claim creation)
            // Must match invoice parameters: wt="1", splitt="04"
            WorkPosition pos1 = createSampleWorkPosition("001", "12345", "20240115", "001", "A", "1", "04");
            WorkPosition pos2 = createSampleWorkPosition("001", "12346", "20240220", "002", "B", "1", "04");
            WorkPosition pos3 = createSampleWorkPosition("002", "12347", "20240310", "003", "A", "1", "04");
            
            workPositionRepository.saveAll(Arrays.asList(pos1, pos2, pos3));
            System.out.println("Created 3 sample work positions");
            
            // Create sample claims from invoices
            Claim claim1 = claimCreationService.createClaimFromInvoice(
                "001", "12345", "20240115", "001", "A");
            Claim claim2 = claimCreationService.createClaimFromInvoice(
                "001", "12346", "20240220", "002", "B");
            Claim claim3 = claimCreationService.createClaimFromInvoice(
                "002", "12347", "20240310", "003", "A");
            
            // Update claim details with more realistic data
            claim1.setClaimNr("00000001");
            claim1.setChassisNr("1234567");
            claim1.setKennzeichen("ABC-123");
            claim1.setZulDatum(20200101);
            claim1.setRepDatum(20240120);
            claim1.setKmStand(50); // in thousands
            claim1.setProduktTyp(1);
            claim1.setKdNr("001001");
            claim1.setKdName("Scania Customer 1");
            claim1.setStatusCodeSde(10);
            claim1.setAnzFehler(2);
            
            claim2.setClaimNr("00000002");
            claim2.setChassisNr("9876543");
            claim2.setKennzeichen("XYZ-789");
            claim2.setZulDatum(20200201);
            claim2.setRepDatum(20240225);
            claim2.setKmStand(75); // in thousands
            claim2.setProduktTyp(1);
            claim2.setKdNr("001002");
            claim2.setKdName("Scania Customer 2");
            claim2.setStatusCodeSde(20);
            claim2.setAnzFehler(1);
            
            claim3.setClaimNr("00000003");
            claim3.setChassisNr("5556667");
            claim3.setKennzeichen("DEF-456");
            claim3.setZulDatum(20200301);
            claim3.setRepDatum(20240315);
            claim3.setKmStand(30); // in thousands
            claim3.setProduktTyp(1);
            claim3.setKdNr("002001");
            claim3.setKdName("Scania Customer 3");
            claim3.setStatusCodeSde(30);
            claim3.setAnzFehler(3);
            
            claimRepository.saveAll(Arrays.asList(claim1, claim2, claim3));
            System.out.println("Created 3 sample claims");
            
            // Create sample claim errors
            ClaimError error1 = createSampleClaimError(claim1, "001", "00000001", "01", "Engine failure");
            ClaimError error2 = createSampleClaimError(claim1, "001", "00000001", "02", "Transmission issue");
            ClaimError error3 = createSampleClaimError(claim2, "001", "00000002", "01", "Brake system problem");
            ClaimError error4 = createSampleClaimError(claim3, "002", "00000003", "01", "Electrical fault");
            ClaimError error5 = createSampleClaimError(claim3, "002", "00000003", "02", "Cooling system issue");
            ClaimError error6 = createSampleClaimError(claim3, "002", "00000003", "03", "Suspension problem");
            
            claimErrorRepository.saveAll(Arrays.asList(error1, error2, error3, error4, error5, error6));
            System.out.println("Created 6 sample claim errors");
            
            // Create sample warranty releases
            WarrantyRelease release1 = createSampleWarrantyRelease("001", "12345", "20240115", "FG123456789012345");
            WarrantyRelease release2 = createSampleWarrantyRelease("001", "12346", "20240220", "FG987654321098765");
            
            warrantyReleaseRepository.saveAll(Arrays.asList(release1, release2));
            System.out.println("Created 2 sample warranty releases");
            
            System.out.println("✅ Sample data initialization completed!");
            System.out.println("   - 3 Invoices");
            System.out.println("   - 3 Claims (CLM001, CLM002, CLM003)");
            System.out.println("   - 6 Claim Errors");
            System.out.println("   - 2 Warranty Releases");
            System.out.println("   - 3 Work Positions");
        };
    }
    
    private Invoice createSampleInvoice(String pakz, String rnr, String rdat, String anr, String berei, String wt, String splitt) {
        Invoice invoice = new Invoice();
        invoice.setPakz(pakz);
        invoice.setRnr(rnr);
        invoice.setRdat(rdat);
        invoice.setAnr(anr);
        invoice.setBerei(berei);
        invoice.setWt(wt);
        invoice.setSplitt(splitt);
        invoice.setAdat(rdat);
        invoice.setAtext("Sample Invoice " + rnr);
        invoice.setRgsNetto(new BigDecimal("5000.00"));
        invoice.setRgsMwst(new BigDecimal("950.00"));
        invoice.setRgsGesBrutto(new BigDecimal("5950.00"));
        // Customer number must be max 6 characters (KUNDEN-NR. column is length 6)
        String customerNr = pakz + "001";
        if (customerNr.length() > 6) {
            customerNr = customerNr.substring(0, 6);
        }
        invoice.setKundenNr(customerNr);
        invoice.setName("Customer " + pakz);
        invoice.setKz("ABC123");
        invoice.setTyp("Truck");
        invoice.setBj("2020");
        invoice.setZdat(rdat);
        invoice.setFahrgNr("FG" + rnr + "1234567");
        invoice.setKm("50000");
        invoice.setAnTag(rdat);
        return invoice;
    }
    
    private ClaimError createSampleClaimError(Claim claim, String pakz, String claimNr, String fehlerNr, String description) {
        ClaimError error = new ClaimError();
        error.setPakz(pakz);
        error.setRechNr(claim.getRechNr());
        error.setRechDatum(claim.getRechDatum());
        error.setAuftragsNr(claim.getAuftragsNr());
        error.setBereich(claim.getBereich());
        error.setClaimNr(claimNr);
        error.setFehlerNr(fehlerNr);
        error.setFolgeNr("01");
        error.setFehlerTeil("ENGINE");
        error.setHauptgruppe("01");
        error.setNebengruppe("01");
        error.setText1(description);
        error.setText2("Additional details for " + description);
        return error;
    }
    
    private WarrantyRelease createSampleWarrantyRelease(String kzl, String rNr, String rDat, String fgnr) {
        WarrantyRelease release = new WarrantyRelease();
        release.setKzl(kzl);
        release.setrNr(rNr);
        release.setrDat(rDat);
        release.setFgnr(fgnr);
        release.setRepDat(rDat);
        release.setStatus("A");
        release.setCusNo(1000);
        release.setDcNo(12345678);
        release.setDcFn("FN001");
        return release;
    }
    
    private WorkPosition createSampleWorkPosition(String pakz, String rnr, String rdat, String anr, String berei, String wt, String splitt) {
        WorkPosition pos = new WorkPosition();
        pos.setPakz(pakz);
        pos.setRnr(rnr);
        pos.setRdat(rdat);
        pos.setAnr(anr);
        pos.setBerei(berei);
        pos.setWt(wt);
        pos.setSplitt(splitt);
        pos.setPos(1);
        pos.setEc("01"); // EC column is only 2 characters
        pos.setLnrPak(1);
        pos.setPaketNr("001"); // PAKET-NR. is length 8, but keep it short
        pos.setBez("Sample Work Position");
        pos.setPreis(new BigDecimal("100.00"));
        return pos;
    }
}
