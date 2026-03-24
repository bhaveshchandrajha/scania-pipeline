```java
package com.scania.warranty.claim.service;

import com.scania.warranty.claim.entity.*;
import com.scania.warranty.claim.repository.*;
import com.scania.warranty.claim.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Migration of RPG Subroutine n514
 * Handles claim processing operations including:
 * - Subfile management (SB10N, SB100)
 * - Claim operations (create, update, delete, display)
 * - Position management
 * - Text and attachment handling
 * - Campaign processing
 * - Model claim creation
 */
@Service
@Transactional
public class ClaimProcessingService {

    // Repository dependencies
    private final HSG71Repository hsg71Repository;
    private final HSG73Repository hsg73Repository;
    private final HSGPSRepository hsgpsRepository;
    private final AUFWSKRepository aufwskRepository;
    private final FARSTLRepository farstlRepository;
    private final S3F004Repository s3f004Repository;
    private final S3F103Repository s3f103Repository;
    private final S3F104Repository s3f104Repository;
    private final S3F105Repository s3f105Repository;
    private final S3F106Repository s3f106Repository;

    // Service dependencies
    private final ClaimService claimService;
    private final TextService textService;
    private final ValidationService validationService;
    private final EpsService epsService;
    private final CampaignService campaignService;

    // Working variables (state)
    private ClaimContext context;

    public ClaimProcessingService(
            HSG71Repository hsg71Repository,
            HSG73Repository hsg73Repository,
            HSGPSRepository hsgpsRepository,
            AUFWSKRepository aufwskRepository,
            FARSTLRepository farstlRepository,
            S3F004Repository s3f004Repository,
            S3F103Repository s3f103Repository,
            S3F104Repository s3f104Repository,
            S3F105Repository s3f105Repository,
            S3F106Repository s3f106Repository,
            ClaimService claimService,
            TextService textService,
            ValidationService validationService,
            EpsService epsService,
            CampaignService campaignService) {
        this.hsg71Repository = hsg71Repository;
        this.hsg73Repository = hsg73Repository;
        this.hsgpsRepository = hsgpsRepository;
        this.aufwskRepository = aufwskRepository;
        this.farstlRepository = farstlRepository;
        this.s3f004Repository = s3f004Repository;
        this.s3f103Repository = s3f103Repository;
        this.s3f104Repository = s3f104Repository;
        this.s3f105Repository = s3f105Repository;
        this.s3f106Repository = s3f106Repository;
        this.claimService = claimService;
        this.textService = textService;
        this.validationService = validationService;
        this.epsService = epsService;
        this.campaignService = campaignService;
    }

    /**
     * SB10N - Initialize subfile processing
     */
    public void sb10n(ClaimContext ctx) {
        ctx.setZl4(0);
        ctx.clearIndicators(50, 52);
        ctx.clearIndicators(53, 55);
        ctx.clearIndicators(56, 58);
        ctx.clearIndicators(59, 63, 74);
        ctx.clearIndicator(76);

        if (ctx.getMark12() == null || ctx.getMark12().isBlank()) {
            ctx.setMark12(ctx.getMark11());
            ctx.setMark11(" ");
        }
    }

    /**
     * MARK - Process selection mark conversion '1 ' -> ' 1'
     */
    public void mark(ClaimContext ctx) {
        if (ctx.getMark12() == null || ctx.getMark12().isBlank()) {
            ctx.setMark12(ctx.getMark11());
            ctx.setMark11(" ");
        }
    }

    /**
     * SB100 - Subfile processing: delete, create, display
     */
    public void sb100(ClaimContext ctx) {
        ctx.clearIndicators(90, 71);
        ctx.setIndicator(99, true);
        ctx.setZl1(0);
        
        // Write control record
        ctx.writeRecord("HS1212C1");
        ctx.setIndicator(99, false);

        // Build key for HSG73
        HSG73Key keyG71 = buildKeyG71(ctx);
        
        List<HSG73Entity> claims = hsg73Repository.findByKey(keyG71);
        ctx.setIndicator(80, claims.isEmpty());

        for (HSG73Entity claim : claims) {
            SubfileRecord subRec = new SubfileRecord();
            subRec.setMark1("");
            subRec.setSub060(claim.getG73060());
            subRec.setSub065(claim.getG73065());
            subRec.setSub070(claim.getG73070());

            // Handle campaign display
            if (claim.getG73280() != null && !claim.getG73280().isBlank() 
                && !claim.getG73280().equals("0") 
                && !claim.getG73280().trim().equals("0")) {
                subRec.setSub070("KAMPAGNE" + claim.getG73280());
            }

            subRec.setSub080(claim.getG73080());
            subRec.setSub100(claim.getG73100());
            subRec.setSub110(claim.getG73110());
            subRec.setSub140(claim.getG73140());
            subRec.setSub180(claim.getG73180());
            subRec.setSub190(claim.getG73190());
            subRec.setSub200(claim.getG73200());
            subRec.setSub240(claim.getG73240());
            subRec.setSub280(claim.getG73280());
            subRec.setSubsta(claim.getG73290().toString());

            srwert(ctx, claim);
            srrot(ctx, claim);
            srgut(ctx, claim);

            ctx.incrementZl1();
            ctx.writeSubfileRecord("HS1212S1", subRec);
        }

        // If no records, write empty line
        if (ctx.getZl1() == 0) {
            SubfileRecord emptyRec = new SubfileRecord();
            emptyRec.setMark1("");
            ctx.setIndicator(71, true);
            ctx.incrementZl1();
            ctx.writeSubfileRecord("HS1212S1", emptyRec);
        }
    }

    /**
     * SR102 - Selection 2: Change claim
     */
    public void sr102(ClaimContext ctx) {
        ctx.setSr102f("Y");
        ctx.setG7305a(ctx.getG71050());
        ctx.setG7306a(ctx.getSub060());

        HSG73Key keyG73 = buildKeyG73(ctx);
        Optional<HSG73Entity> claimOpt = hsg73Repository.findByKey(keyG73);
        ctx.setIndicator(81, claimOpt.isEmpty());

        if (claimOpt.isPresent()) {
            HSG73Entity claim = claimOpt.get();
            
            // Reset status if 30 or 16
            if (claim.getG73290() == 30 || claim.getG73290() == 16) {
                claim.setG73290(0);
                hsg73Repository.save(claim);

                // Delete errors from SDE001
                deleteClaimErrors(ctx);

                // Check if any claim has status > 0
                boolean hasActiveStatus = checkActiveClaimStatus(ctx);
                
                if (hasActiveStatus && ctx.getG71160().isBlank()) {
                    updateClaimHeaderStatus(ctx, 2);
                }
            }

            claimService.loadClaim(ctx);
            sr06(ctx);
            ctx.setSr102f("");
        }
    }

    /**
     * SR104 - Selection 4: Delete claim
     */
    public void sr104(ClaimContext ctx) {
        ctx.setZl4(0);
        ctx.clearIndicator(90);
        ctx.setIndicator(99, true);
        ctx.writeRecord("HS1212C4");
        ctx.setIndicator(99, false);

        List<SubfileRecord> records = ctx.readChangedSubfile("HS1212S1");
        
        for (SubfileRecord rec : records) {
            mark(ctx);
            
            if (" 4".equals(rec.getMark1())) {
                ctx.incrementZl4();
                if (ctx.getZl4() == 1) {
                    ctx.setPag1(ctx.getZl1());
                }
                rec.setMark4(" 4");
                ctx.writeSubfileRecord("HS1212S4", rec);
            }

            ctx.setIndicator(93, true);
            srrot(ctx, null);
            ctx.updateSubfileRecord("HS1212S1", rec);
            ctx.setIndicator(93, false);
        }

        ctx.setIndicator(90, true);
        ctx.writeRecord("HS121240");
        ctx.displayRecord("HS1212C4");

        if (ctx.isIndicator(12)) {
            sb100(ctx);
            return;
        }

        // Process deletions
        records = ctx.readChangedSubfile("HS1212S1");
        for (SubfileRecord rec : records) {
            mark(ctx);
            
            if (" 4".equals(rec.getMark1())) {
                ctx.setG7305a(ctx.getG71050());
                ctx.setG7306a(rec.getSub060());
                
                HSG73Key keyG73 = buildKeyG73(ctx);
                Optional<HSG73Entity> claimOpt = hsg73Repository.findByKey(keyG73);
                
                if (claimOpt.isPresent()) {
                    hsg73Repository.delete(claimOpt.get());
                    
                    // Decrement error count
                    decrementErrorCount(ctx);
                    
                    // Delete from HSGPTF
                    deleteClaimText(ctx);
                    
                    // Delete positions
                    deleteClaimPositions(ctx, rec);
                }
            } else {
                ctx.setIndicator(93, true);
                srrot(ctx, null);
                ctx.updateSubfileRecord("HS1212S1", rec);
                ctx.setIndicator(93, false);
            }
        }

        ctx.clearIndicators(3, 12);
    }

    /**
     * SR105 - Selection 5: Display claim
     */
    public void sr105(ClaimContext ctx) {
        HSG73Key key = new HSG73Key(ctx.getG71000(), ctx.getG71050(), ctx.getSub060());
        Optional<HSG73Entity> claimOpt = hsg73Repository.findByKey(key);
        
        if (claimOpt.isPresent()) {
            claimService.loadClaim(ctx);
            HSG73Entity claim = claimOpt.get();
            
            if (claim.getG73280() == null || claim.getG73280().isBlank()) {
                while (!ctx.isIndicator(12)) {
                    ctx.displayRecord("HS1212W5");
                    
                    if (ctx.isIndicator(4)) {
                        ctx.setIndicator(78, true);
                        ctx.setFlag04("HS1212WT");
                        textService.loadText(ctx, "GPT");
                        sr04(ctx);
                        ctx.setIndicator(78, false);
                    }
                }
            } else {
                ctx.setIndicator(25, true);
                ctx.displayRecord("HS1212WK");
                ctx.setIndicator(25, false);
            }
        }
    }

    /**
     * SR106 - Selection 6: Display positions
     */
    public void sr106(ClaimContext ctx) {
        ctx.setMark1("");
        srrot(ctx, null);
        ctx.updateSubfileRecord("HS1212S1", null);
        
        String art = "5";
        if ("00".equals(ctx.getSubsta()) || "16".equals(ctx.getSubsta())) {
            art = "2";
        }
        
        claimService.callHS1215(art, ctx.getG7100p(), ctx.getG7101p(), 
            ctx.getG7102p(), ctx.getG7103p(), ctx.getG7104p(), 
            ctx.getG71050(), ctx.getSub060(), ctx.getAktion());
    }

    /**
     * SR107 - Selection 7: Assign positions
     */
    public void sr107(ClaimContext ctx) {
        ctx.setMark1("");
        srrot(ctx, null);
        ctx.updateSubfileRecord("HS1212S1", null);
        
        ctx.setAuto("");
        claimService.callHS1217(ctx.getG7100p(), ctx.getG7101p(), 
            ctx.getG7102p(), ctx.getG7103p(), ctx.getG7104p(), 
            ctx.getG71050(), ctx.getSub060(), ctx.getAktion(), ctx.getAuto());
    }

    /**
     * SR108 - Selection 8: Attachments
     */
    public void sr108(ClaimContext ctx) {
        claimService.callHS1228(ctx.getG7100p(), ctx.getG71050(), 
            ctx.getSub060(), ctx.getSub065(), ctx.getG7111a(), 
            ctx.getG71060(), ctx.getSubsta());
    }

    /**
     * SR109 - Selection 9: Print claim
     */
    public void sr109(ClaimContext ctx) {
        claimService.printClaim(ctx.getG71000(), ctx.getG71050(), 
            ctx.getSub060(), ctx.getSub065(), "");
    }

    /**
     * SR110 - Selection 10: Send claim
     */
    public void sr110(ClaimContext ctx) {
        claimService.sendClaim(ctx.getG71000(), ctx.getG71050(), 
            ctx.getSub060(), ctx.getSub065());
        
        if (!"ZZZ".equals(ctx.getG71000())) {
            try {
                ctx.setG7305a(ctx.getG71050());
                ctx.setG7306a(ctx.getSub060());
                
                HSG73Key keyG73 = buildKeyG73(ctx);
                Optional<HSG73Entity> claimOpt = hsg73Repository.findByKey(keyG73);
                
                if (claimOpt.isPresent()) {
                    HSG73Entity claim = claimOpt.get();
                    if (claim.getG73240() == 9) {
                        // Email notification logic would go here
                    }
                }
            } catch (Exception e) {
                // Log error
            }
        }
    }

    /**
     * SR112 - Selection 12: Edit claim in SCAS
     */
    public void sr112(ClaimContext ctx) {
        ctx.setS3fCno(new BigDecimal(ctx.getG71160()));
        ctx.setS3fFno(new BigDecimal(ctx.getSub060()));
        ctx.setS3fDno(ctx.getKey0011());
        
        S3F004Key key = new S3F004Key(ctx.getS3fCno(), ctx.getS3fFno(), ctx.getS3fDno());
        Optional<S3F004Entity> s3fOpt = s3f004Repository.findByKey(key);
        
        if (s3fOpt.isPresent()) {
            claimService.callHS1212C2(ctx.getG71160(), ctx.getSub060(), 
                ctx.getKey0011().toString());
        }
    }

    /**
     * SR115 - Selection 15: Display claim in SCAS
     */
    public void sr115(ClaimContext ctx) {
        ctx.setG7305a(ctx.getG71050());
        ctx.setG7306a(ctx.getSub060());
        
        HSG73Key keyG73 = buildKeyG73(ctx);
        Optional<HSG73Entity> claimOpt = hsg73Repository.findByKey(keyG73);
        
        if (claimOpt.isPresent()) {
            HSG73Entity claim = claimOpt.get();
            
            if (validationService.isWarrScope(claim.getG73140()) 
                && ctx.getG71190() != null && !ctx.getG71190().isBlank()) {
                displayWarrantyClaimInfo(ctx, claim);
            } else {
                displayStandardClaimInfo(ctx, claim);
            }
        }
    }

    /**
     * SR117 - Selection 17: Display credit note
     */
    public void sr117(ClaimContext ctx) {
        if (ctx.getSubgut() != null && !ctx.getSubgut().isBlank()) {
            String scope = validationService.getScope(ctx.getSub140());
            String gutart;
            
            if (scope.startsWith("A")) {
                gutart = "A";
                claimService.callHs1212C3(gutart, ctx.getSubgut());
            } else if (scope.startsWith("R")) {
                gutart = "R";
                claimService.callHs1212C3(gutart, ctx.getSubgut());
            } else {
                gutart = "D";
                claimService.callHs1295(gutart, ctx.getSubgut(), ctx.getReprint());
                
                try {
                    claimService.displaySplf();
                    claimService.deleteSplf();
                } catch (Exception e) {
                    // Log error
                }
            }
        }
    }

    /**
     * SR120 - Selection 20: Follow-up claim
     */
    public void sr120(ClaimContext ctx) {
        LocalDate vgldat2 = ctx.getAktdat().minusDays(ctx.getTage());
        
        if (validationService.isWarrScope(ctx.getSub140())) {
            if (Integer.parseInt(ctx.getSubsta()) > 3) {
                LocalDate vgldat1 = ctx.getAktdat().minusDays(ctx.getTage());
                ctx.setVgldat1(vgldat1);
                sr120a(ctx);
            }
        } else {
            // Check time limit (14 days from credit date)
            processFollowUpClaim(ctx, vgldat2);
        }
    }

    /**
     * SR120A - Save follow-up claim to files
     */
    public void sr120a(ClaimContext ctx) {
        if (ctx.getVgldat2().isAfter(ctx.getVgldat1())) {
            if (!checkClaimStatus(ctx)) {
                ctx.displayRecord("HS1212WZ");
                return;
            }
        }

        String kopiecnr = ctx.getG71050();
        int sub060n = Integer.parseInt(ctx.getSub060());
        
        // Find next available error number
        HSG73Key key = new HSG73Key(ctx.getG7100p(), ctx.getG71050(), 
            String.format("%02d", sub060n));
        
        while (hsg73Repository.findByKey(key).isPresent() && sub060n < 100) {
            sub060n += 10;
            key = new HSG73Key(ctx.getG7100p(), ctx.getG71050(), 
                String.format("%02d", sub060n));
        }

        if (hsg73Repository.findByKey(key).isPresent() || sub060n > 99) {
            ctx.displayRecord("HS1212WM");
        } else {
            createFollowUpClaim(ctx, kopiecnr, sub060n);
        }
    }

    /**
     * SR04 - Command key 04: User guidance
     */
    public void sr04(ClaimContext ctx) {
        ctx.setF11txt("");
        
        if ("HS1212W6".equals(ctx.getFlag04()) && "C4 ".equals(ctx.getFlagx4())) {
            ctx.setF11txt(ctx.getF110());
        }

        while (!ctx.isIndicator(12)) {
            ctx.clearIndicator(90);
            ctx.setIndicator(99, true);
            ctx.setZla(0);
            ctx.writeRecord("HS1212CA");
            ctx.setIndicator(99, false);

            processUserGuidance(ctx);

            if (ctx.getZla() == 0 && "C4".equals(ctx.getFlagx4()) 
                && ctx.getF11txt().equals(ctx.getF110())) {
                ctx.setF11txt(ctx.getF111());
                continue;
            }

            if (ctx.getZla() != 0 && !ctx.isIndicator(12)) {
                ctx.setIndicators(90, 95, true);
                ctx.writeRecord("HS1212WA");
                ctx.displayRecord("HS1212CA");
                ctx.setIndicators(90, 95, false);

                if (ctx.isIndicator(11)) {
                    ctx.setF11txt(ctx.getF11txt().equals(ctx.getF110()) 
                        ? ctx.getF111() : ctx.getF110());
                    continue;
                }

                processUserSelection(ctx);
            }
            
            ctx.setIndicator(12, true);
        }

        ctx.setIndicator(12, false);
        claimService.getDescription(ctx);
    }

    /**
     * SR06 - Command key 6: Create claim
     */
    public void sr06(ClaimContext ctx) {
        if (ctx.getSr102f() == null || ctx.getSr102f().isBlank()) {
            initializeClaimFields(ctx);
        }

        ctx.setEpsCheck(false);
        ctx.clearIndicators(31, 33);
        ctx.clearIndicators(34, 36);
        ctx.clearIndicators(37, 39);
        ctx.clearIndicators(44, 47);
        ctx.setIndicator(32, true);
        ctx.clearIndicators(41, 43);

        // Determine error number
        if (ctx.getSr102f() == null || ctx.getSr102f().isBlank()) {
            int fnr = determineNextErrorNumber(ctx);
            
            if (fnr > 9) {
                ctx.displayRecord("HS1212WN");
                if (ctx.isIndicator(12)) {
                    ctx.setIndicator(12, false);
                    return;
                }
                
                ctx.setWelche("N");
                ctx.displayRecord("HS1212WO");
                if (ctx.isIndicator(12)) {
                    ctx.setIndicator(12, false);
                    return;
                }
                
                srneu(ctx);
            }

            // Check for existing campaign
            srKampagnen(ctx);
            if (ctx.getReturnValue() != null && !ctx.getReturnValue().isBlank()) {
                return;
            }

            if (ctx.isIndicator(40)) {
                return;
            }
            if (ctx.isIndicator(12)) {
                return;
            }
        } else {
            ctx.setFnr(Integer.parseInt(ctx.getG7306a()));
        }

        // Validate and process claim
        processClaimCreation(ctx);
    }

    /**
     * SR08 - Command key 08: Display warranty info
     */
    public void sr08(ClaimContext ctx) {
        ctx.setFgnr17(ctx.getG71060());
        claimService.callHS0069C(ctx.getFgnr17());
    }

    /**
     * SR16 - Command key 16: New claim
     */
    public void sr16(ClaimContext ctx) {
        ctx.setWelche("N");
        ctx.displayRecord("HS1212WO");
        
        if (ctx.isIndicator(12)) {
            ctx.setIndicator(12, false);
            return;
        }
        
        srneu(ctx);
    }

    /**
     * SR18 - Command key 18: Change header data
     */
    public void sr18(ClaimContext ctx) {
        prepareHeaderData(ctx);
        
        while (true) {
            if (ctx.getG71170() > 5) {
                ctx.displayRecord("HS1212A8");
            } else {
                ctx.displayRecord("HS1212F8");
            }

            if (ctx.isIndicator(3) || (ctx.isIndicator(12) && "J".equals(ctx.getMinimum())) 
                || ctx.getG71170() > 5) {
                return;
            }

            if (ctx.isIndicator(12)) {
                if (hasValidationErrors(ctx)) {
                    return;
                } else {
                    return;
                }
            }

            if (ctx.isIndicator(60)) {
                continue;
            }

            ctx.clearIndicator(49);
            ctx.clearIndicators(50, 52);
            ctx.clearIndicators(53, 55);
            srchk1(ctx);

            if (hasValidationErrors(ctx)) {
                if (ctx.isIndicator(12)) {
                    return;
                }
                continue;
            }

            if (ctx.isIndicator(12)) {
                return;
            }

            updateHeaderData(ctx);
            
            if ("00000000".equals(ctx.getG71160())) {
                return;
            }
            
            return;
        }
    }

    /**
     * SR_UPDGPS - Update percentages in HSGPSPF when demand code changes
     */
    public void srUpdgps(ClaimContext ctx) {
        List<HSGPSEntity> positions = hsgpsRepository.findByKey(buildKeyGPS(ctx));
        
        for (HSGPSEntity pos : positions) {
            if (pos.getGps020() != null && !pos.getGps020().isBlank()) {
                int gps120n;
                
                switch (pos.getGps040()) {
                    case "ARB":
                        gps120n = ctx.getG73190();
                        break;
                    case "MAT":
                        gps120n = ctx.getG73180();
                        break;
                    default:
                        gps120n = ctx.getG73200();
                        break;
                }

                if (gps120n != pos.getGps120()) {
                    pos.setGps120(gps120n);
                    hsgpsRepository.save(pos);
                }
            }
        }
    }

    /**
     * SRDAT - Format date
     */
    public void srdat(ClaimContext ctx) {
        ctx.setPunkt1(".");
        ctx.setPunkt2(".");
        ctx.setDatott(ctx.getDatitt());
        ctx.setDatomm(ctx.getDatimm());
        ctx.setDatojj(ctx.getDatijj());
        
        if ("0000".equals(ctx.getDatojj())) {
            ctx.setDatout("");
        }
    }

    /**
     * SRDATN - Format date numerically
     */
    public void srdatn(ClaimContext ctx) {
        ctx.setDatnoj(ctx.getDatnij());
        ctx.setDatnom(ctx.getDatnim());
        ctx.setDatnot(ctx.getDatnit());
    }

    /**
     * SRTNR - Format part number
     */
    public void srtnr(ClaimContext ctx) {
        String tnr7 = ctx.getG7307a().substring(0, 7);
        String[] tnr = new String[7];
        String[] tnra = new String[7];
        
        tnr[6] = String.valueOf(ctx.getTnr71());
        tnr[5] = String.valueOf(ctx.getTnr72());
        tnr[4] = String.valueOf(ctx.getTnr73());
        tnr[3] = String.valueOf(ctx.getTnr74());
        tnr[2] = String.valueOf(ctx.getTnr75());
        tnr[1] = String.valueOf(ctx.getTnr76());
        tnr[0] = String.valueOf(ctx.getTnr77());
        
        Arrays.fill(tnra, "0");
        
        int y = 6;
        for (int x = 0; x < 7; x++) {
            if (!tnr[x].isBlank()) {
                tnra[y] = tnr[x];
                y--;
            }
        }

        ctx.setTnr71(tnra[0].charAt(0));
        ctx.setTnr72(tnra[1].charAt(0));
        ctx.setTnr73(tnra[2].charAt(0));
        ctx.setTnr74(tnra[3].charAt(0));
        ctx.setTnr75(tnra[4].charAt(0));
        ctx.setTnr76(tnra[5].charAt(0));
        ctx.setTnr77(tnra[6].charAt(0));
        
        ctx.setG7307a(String.join("", tnra));
    }

    /**
     * SRERR1 - Error routine 1: Invalid selection
     */
    public void srerr1(ClaimContext ctx) {
        ctx.setErr(0);
        
        List<SubfileRecord> records = ctx.readChangedSubfile("HS1212S1");
        
        for (SubfileRecord rec : records) {
            mark(ctx);
            
            if (!validateSelection(ctx, rec)) {
                ctx.incrementErr();
                if (ctx.getErr() == 1) {
                    ctx.setRec1(ctx.getZl1());
                    ctx.setPag1(ctx.getZl1());
                    ctx.clearIndicator(30);
                }
                ctx.setIndicators(56, 70, true);
            }

            ctx.setIndicator(93, true);
            ctx.updateSubfileRecord("HS1212S1", rec);
            ctx.setIndicators(70, 93, false);
        }
    }

    /**
     * SRCHK1 - Check header data
     */
    public void srchk1(ClaimContext ctx) {
        // Product type valid?
        if (ctx.getG7111a() < 1 || ctx.getG7111a() > '5') {
            ctx.setIndicator(50, true);
        }

        // Chassis valid?
        if (ctx.getG7106a() != null && !ctx.getG7106a().isBlank()) {
            validateChassis(ctx);
        } else {
            validateNoChassis(ctx);
        }

        // Repair date valid?
        validateRepairDate(ctx);

        // Mileage valid?
        validateMileage(ctx);

        // Update status
        updateClaimStatus(ctx);
    }

    /**
     * SR_Kampagnen - Create campaign positions
     */
    public void srKampagnen(ClaimContext ctx) {
        ctx.setReturnValue("");
        ctx.setG73065("00");
        
        campaignService.callHS1213(ctx.getG71000(), ctx.getG71010(), 
            ctx.getG71020(), ctx.getG71030(), ctx.getBereich(), 
            ctx.getG71040(), ctx.getG71060(), ctx.getG71050(), 
            ctx.getG7306a(), ctx.getG73065(), ctx.getReturnValue());
    }

    /**
     * SRWERT - Calculate claim value
     */
    public void srwert(ClaimContext ctx, HSG73Entity claim) {
        BigDecimal subwert = BigDecimal.ZERO;
        
        List<HSGPSEntity> positions = hsgpsRepository.findByKey(buildKeyGPS(ctx));
        for (HSGPSEntity pos : positions) {
            subwert = subwert.add(pos.getGps070());
        }
        
        ctx.setSubwert(subwert);
    }

    /**
     * SRROT - Display status in red if rejected
     */
    public void srrot(ClaimContext ctx, HSG73Entity claim) {
        ctx.clearIndicator(83);
        
        if (claim != null && (claim.getG73290() == 30 || claim.getG73290() == 16)) {
            ctx.setIndicator(83, true);
        }
    }

    /**
     * SRGUT - Determine credit note number
     */
    public void srgut(ClaimContext ctx, HSG73Entity claim) {
        ctx.setSubgut("");
        ctx.clearIndicator(72);
        
        if (ctx.getG71160() == null || ctx.getG71160().isBlank()) {
            return;
        }

        String scope = validationService.getScope(claim.getG73140());
        
        if (scope.startsWith("A")) {
            findCreditNoteA(ctx, claim);
        } else if (scope.startsWith("R")) {
            findCreditNoteR(ctx, claim);
        } else {
            findCreditNoteStandard(ctx, claim);
        }

        // Yellow for rejected claims
        if (claim.getG73290() > 2 && ctx.getGelb() != null && !ctx.getGelb().isBlank()) {
            checkRejectionStatus(ctx, claim);
        }
    }

    /**
     * SR20 - Copy claim
     */
    public void sr20(ClaimContext ctx) {
        initializeCopySubfile(ctx);
        
        if (allSearchFieldsBlank(ctx)) {
            return;
        }

        fillCopySubfile(ctx);
        displayCopySubfile(ctx);
        
        if (ctx.isIndicator(12)) {
            return;
        }

        processCopySelection(ctx);
    }

    /**
     * SRNEU - Create new claim when error > 09
     */
    public void srneu(ClaimContext ctx) {
        String cn = ctx.getG71050();
        String fn = "0";

        // Find next free number
        List<HSG71Entity> claims = hsg71Repository.findByClaimNumber(cn);
        for (HSG71Entity claim : claims) {
            if (claim.getG71040().compareTo(fn) > 0) {
                fn = claim.getG71040();
            }
        }

        // Increment by 1
        int fnn = Integer.parseInt(fn) + 1;
        fn = String.valueOf(fnn);

        // Check if free number available
        if (checkHighValue(ctx)) {
            return;
        }

        BigDecimal cnr = findNextClaimNumber(ctx);
        
        createNewClaim(ctx, fn, cnr);
    }

    /**
     * SR_ZEILE - Determine next line number
     */
    public void srZeile(ClaimContext ctx) {
        int zeile = 0;
        
        List<HSGPSEntity> positions = hsgpsRepository.findByKey(buildKeyGPS2(ctx));
        for (HSGPSEntity pos : positions) {
            if (pos.getGps030() > zeile) {
                zeile = pos.getGps030();
            }
        }
        
        ctx.setZeile(zeile);
    }

    /**
     * SR_MODEL - Create model claim
     */
    public void srModel(ClaimContext ctx) {
        ctx.setG7312a(ctx.getRsd1ma());
        ctx.setG7313a(ctx.getRsd2ma());
        ctx.setG7332a(ctx.getRsd3ma());
        ctx.setG7333a(ctx.getRsd4ma());

        srZeile(ctx);

        // Create parts
        createModelParts(ctx);

        // Create labor
        createModelLabor(ctx);

        // Create special costs parts
        createModelSpecialParts(ctx);

        // Create special costs labor
        createModelSpecialLabor(ctx);
    }

    /**
     * SR_PRUEFP - Check if positions exist
     */
    public void srPruefp(ClaimContext ctx) {
        ctx.setAnzpos(0);
        ctx.clearIndicator(77);
        
        List<HSGPSEntity> positions = hsgpsRepository.findByKey(buildKeyGPS2(ctx));
        for (HSGPSEntity pos : positions) {
            if ((pos.getGps020() == null || pos.getGps020().isBlank()) 
                && !"TXT".equals(pos.getGps040())) {
                if (ctx.getAnzpos() < 99) {
                    ctx.incrementAnzpos();
                }
                ctx.setIndicator(77, true);
            }
        }
    }

    /**
     * SR_TEXT - Edit damage text
     */
    public void srText(ClaimContext ctx) {
        while (!ctx.isIndicator(12)) {
            ctx.clearIndicator(4);
            ctx.clearIndicator(10);
            ctx.setIndicator(90, true);
            ctx.clearIndicator(99);
            ctx.writeRecord("HS1212WT");
            ctx.displayRecord("HS1212CT");

            if (ctx.isIndicator(4)) {
                textService.loadOrderTextSfl(ctx);
            } else if (ctx.isIndicator(10)) {
                textService.loadText(ctx, "GPS");
            } else if (!ctx.isIndicator(12)) {
                textService.saveText(ctx);
                ctx.setIndicator(12, true);
            }
        }
    }

    // Helper methods

    private HSG73Key buildKeyG71(ClaimContext ctx) {
        return new HSG73Key(ctx.getG7100p(), ctx.getG7101p(), ctx.getG7102p());
    }

    private HSG73Key buildKeyG73(ClaimContext ctx) {
        return new HSG73Key(ctx.getG7100p(), ctx.getG7305a(), ctx.getG7306a());
    }

    private HSGPSKey buildKeyGPS(ClaimContext ctx) {
        return new HSGPSKey(ctx.getG7100p(), ctx.getG71050(), ctx.getG73050());
    }

    private HSGPSKey buildKeyGPS2(ClaimContext ctx) {
        return new HSGPSKey(ctx.getG7100p(), ctx.getG71050());
    }

    private void deleteClaimErrors(ClaimContext ctx) {
        // Implementation for deleting errors from SDE001
    }

    private boolean checkActiveClaimStatus(ClaimContext ctx) {
        List<HSG73Entity> claims = hsg73Repository.findByKey(buildKeyG71(ctx));
        return claims.stream().anyMatch(c -> c.getG73290() > 0);
    }

    private void updateClaimHeaderStatus(ClaimContext ctx, int status) {
        // Implementation for updating claim header status
    }

    private void decrementErrorCount(ClaimContext ctx) {
        // Implementation for decrementing error count
    }

    private void deleteClaimText(ClaimContext ctx) {
        // Implementation for deleting claim text
    }

    private void deleteClaimPositions(ClaimContext ctx, SubfileRecord rec) {
        // Implementation for deleting claim positions
    }

    private void displayWarrantyClaimInfo(ClaimContext ctx, HSG73Entity claim) {
        // Implementation for displaying warranty claim info
    }

    private void displayStandardClaimInfo(ClaimContext ctx, HSG73Entity claim) {
        // Implementation for displaying standard claim info
    }

    private void processFollowUpClaim(ClaimContext ctx, LocalDate vgldat2) {
        // Implementation for processing follow-up claim
    }

    private boolean checkClaimStatus(ClaimContext ctx) {
        // Implementation for checking claim status
        return true;
    }

    private void createFollowUpClaim(ClaimContext ctx, String kopiecnr, int sub060n) {
        // Implementation for creating follow-up claim
    }

    private void processUserGuidance(ClaimContext ctx) {
        // Implementation for processing user guidance
    }

    private void processUserSelection(ClaimContext ctx) {
        // Implementation for processing user selection
    }

    private void initializeClaimFields(ClaimContext ctx) {
        // Implementation for initializing claim fields
    }

    private int determineNextErrorNumber(ClaimContext ctx) {
        // Implementation for determining next error number
        return 0;
    }

    private void processClaimCreation(ClaimContext ctx) {
        // Implementation for processing claim creation
    }

    private void prepareHeaderData(ClaimContext ctx) {
        // Implementation for preparing header data
    }

    private boolean hasValidationErrors(ClaimContext ctx) {
        return ctx.isIndicator(50) || ctx.isIndicator(51) || ctx.isIndicator(52) 
            || ctx.isIndicator(53) || ctx.isIndicator(54) || ctx.isIndicator(49);
    }

    private void updateHeaderData(ClaimContext ctx) {
        // Implementation for updating header data
    }

    private boolean validateSelection(ClaimContext ctx, SubfileRecord rec) {
        // Implementation for validating selection
        return true;
    }

    private void validateChassis(ClaimContext ctx) {
        // Implementation for validating chassis
    }

    private void validateNoChassis(ClaimContext ctx) {
        // Implementation for validating no chassis scenario
    }

    private void validateRepairDate(ClaimContext ctx) {
        // Implementation for validating repair date
    }

    private void validateMileage(ClaimContext ctx) {
        // Implementation for validating mileage
    }

    private void updateClaimStatus(ClaimContext ctx) {
        // Implementation for updating claim status
    }

    private void findCreditNoteA(ClaimContext ctx, HSG73Entity claim) {
        // Implementation for finding credit note type A
    }

    private void findCreditNoteR(ClaimContext ctx, HSG73Entity claim) {
        // Implementation for finding credit note type R
    }

    private void findCreditNoteStandard(ClaimContext ctx, HSG73Entity claim) {
        // Implementation for finding standard credit note
    }

    private void checkRejectionStatus(ClaimContext ctx, HSG73Entity claim) {
        // Implementation for checking rejection status
    }

    private void initializeCopySubfile(ClaimContext ctx) {
        // Implementation for initializing copy subfile
    }

    private boolean allSearchFieldsBlank(ClaimContext ctx) {
        // Implementation for checking if all search fields are blank
        return false;
    }

    private void fillCopySubfile(ClaimContext ctx) {
        // Implementation for filling copy subfile
    }

    private void displayCopySubfile(ClaimContext ctx) {
        // Implementation for displaying copy subfile
    }

    private void processCopySelection(ClaimContext ctx) {
        // Implementation for processing copy selection
    }

    private boolean checkHighValue(ClaimContext ctx) {
        // Implementation for checking high value
        return false;
    }

    private BigDecimal findNextClaimNumber(ClaimContext ctx) {
        // Implementation for finding next claim number
        return BigDecimal.ZERO;
    }

    private void createNewClaim(ClaimContext ctx, String fn, BigDecimal cnr) {
        // Implementation for creating new claim
    }

    private void createModelParts(ClaimContext ctx) {
        List<S3F103Entity> parts = s3f103Repository.findByKey(ctx.getKey101());
        for (S3F103Entity part : parts) {
            // Create position for each part
        }
    }

    private void createModelLabor(ClaimContext ctx) {
        List<S3F104Entity> labor = s3f104Repository.findByKey(ctx.getKey101());
        for (S3F104Entity lab : labor) {
            // Create position for each labor
        }
    }

    private void createModelSpecialParts(ClaimContext ctx) {
        List<S3F105Entity> specialParts = s3f105Repository.findByKey(ctx.getKey101());
        for (S3F105Entity sp : specialParts) {
            // Create position for each special part
        }
    }

    private void createModelSpecialLabor(ClaimContext ctx) {
        List<S3F106Entity> specialLabor = s3f106Repository.findByKey(ctx.getKey101());
        for (S3F106Entity sl : specialLabor) {
            // Create position for each special labor
        }
    }
}

/**
 * Context object holding claim processing state
 */
class ClaimContext {
    // All working variables from RPG
    private int zl1, zl4, zlw2, err, anzpos, zeile, fnr;
    private BigDecimal zwrec2, rec2, pag2, rec1, pag1;
    private String mark11, mark12, mark1, sr102f, sub060, sub065, substa;
    private String g7100p, g7101p, g7102p, g7103p, g7104p;
    private String g71000, g71010, g71020, g71030, g71040, g71050, g71060, g71070;
    private String g71160, g71190, g7305a, g7306a, g7307a, g7308a, g7310a;
    private String g7311a, g7312a, g7313a, g7314a, g7324a, g7328a;
    private String g7332a, g7333a, g7335a, g7106a, g7107a;
    private String g73050, g73065, g73070, g73080, g73090, g73100, g73110;
    private String g73120, g73130, g73140, g73280, g73340;
    private int g71090, g71100, g71170, g73180, g73190, g73200, g73240, g73290;
    private int g7318a, g7319a, g7320a, g7326a;
    private BigDecimal g7327a, g73210, g73220, g73230, g73260, g73270;
    private String aktion, art, auto, bereich, wete, splitt, returnValue;
    private String fgnr17, welche, minimum, subgut, gelb;
    private String flag04, flagx4, f11txt, f110, f111;
    private LocalDate aktdat, vgldat1, vgldat2;
    private int tage;
    private boolean epsCheck;
    private BigDecimal subwert, s3fCno, s3fFno, s3fDno, key0011;
    private String datitt, datimm, datijj, datott, datomm, datojj, datout;
    private String datnij, datnim, datnit, datnoj, datnom, datnot;
    private String punkt1, punkt2;
    private char tnr71, tnr72, tnr73, tnr74, tnr75, tnr76, tnr77;
    private String rsd1ma, rsd2ma, rsd3ma, rsd4ma;
    private int zla;
    private String reprint;
    private Map<Integer, Boolean> indicators = new HashMap<>();

    // Getters and setters for all fields
    public int getZl1() { return zl1; }
    public void setZl1(int zl1) { this.zl1 = zl1; }
    public void incrementZl1() { this.zl1++; }
    
    public int getZl4() { return zl4; }
    public void setZl4(int zl4) { this.zl4 = zl4; }
    public void incrementZl4() { this.zl4++; }
    
    public int getErr() { return err; }
    public void setErr(int err) { this.err = err; }
    public void incrementErr() { this.err++; }
    
    public int getAnzpos() { return anzpos; }
    public void setAnzpos(int anzpos) { this.anzpos = anzpos; }
    public void incrementAnzpos() { this.anzpos++; }
    
    public String getMark11() { return mark11; }
    public void setMark11(String mark11) { this.mark11 = mark11; }
    
    public String getMark12() { return mark12; }
    public void setMark12(String mark12) { this.mark12 = mark12; }
    
    public String getMark1() { return mark1; }
    public void setMark1(String mark1) { this.mark1 = mark1; }
    
    public String getSr102f() { return sr102f; }
    public void setSr102f(String sr102f) { this.sr102f = sr102f; }
    
    public String getSub060() { return sub060; }
    public void setSub060(String sub060) { this.sub060 = sub060; }
    
    public String getSub065() { return sub065; }
    public void setSub065(String sub065) { this.sub065 = sub065; }
    
    public String getSubsta() { return substa; }
    public void setSubsta(String substa) { this.substa = substa; }
    
    public String getG7100p() { return g7100p; }
    public void setG7100p(String g7100p) { this.g7100p = g7100p; }
    
    public String getG7101p() { return g7101p; }
    public void setG7101p(String g7101p) { this.g7101p = g7101p; }
    
    public String getG7102p() { return g7102p; }
    public void setG7102p(String g7102p) { this.g7102p = g7102p; }
    
    public String getG7103p() { return g7103p; }
    public void setG7103p(String g7103p) { this.g7103p = g7103p; }
    
    public String getG7104p() { return g7104p; }
    public void setG7104p(String g7104p) { this.g7104p = g7104p; }
    
    public String getG71000() { return g71000; }
    public void setG71000(String g71000) { this.g71000 = g71000; }
    
    public String getG71050() { return g71050; }
    public void setG71050(String g71050) { this.g71050 = g71050; }
    
    public String getG71060() { return g71060; }
    public void setG71060(String g71060) { this.g71060 = g71060; }
    
    public String getG71160() { return g71160; }
    public void setG71160(String g71160) { this.g71160 = g71160; }
    
    public String getG71190() { return g71190; }
    public void setG71190(String g71190) { this.g71190 = g71190; }
    
    public int getG71090() { return g71090; }
    public void setG71090(int g71090) { this.g71090 = g71090; }
    
    public int getG71100() { return g71100; }
    public void setG71100(int g71100) { this.g71100 = g71100; }
    
    public int getG71170() { return g71170; }
    public void setG71170(int g71170) { this.g71170 = g71170; }
    
    public String getG7305a() { return g7305a; }
    public void setG7305a(String g7305a) { this.g7305a = g7305a; }
    
    public String getG7306a() { return g7306a; }
    public void setG7306a(String g7306a) { this.g7306a = g7306a; }
    
    public String getG7307a() { return g7307a; }
    public void setG7307a(String g7307a) { this.g7307a = g7307a; }
    
    public String getG7308a() { return g7308a; }
    public void setG7308a(String g7308a) { this.g7308a = g7308a; }
    
    public String getG7310a() { return g7310a; }
    public void setG7310a(String g7310a) { this.g7310a = g7310a; }
    
    public String getG7312a() { return g7312a; }
    public void setG7312a(String g7312a) { this.g7312a = g7312a; }
    
    public String getG7313a() { return g7313a; }
    public void setG7313a(String g7313a) { this.g7313a = g7313a; }
    
    public String getG7314a() { return g7314a; }
    public void setG7314a(String g7314a) { this.g7314a = g7314a; }
    
    public String getG7324a() { return g7324a; }
    public void setG7324a(String g7324a) { this.g7324a = g7324a; }
    
    public String getG7332a() { return g7332a; }
    public void setG7332a(String g7332a) { this.g7332a = g7332a; }
    
    public String getG7333a() { return g7333a; }
    public void setG7333a(String g7333a) { this.g7333a = g7333a; }
    
    public String getG73050() { return g73050; }
    public void setG73050(String g73050) { this.g73050 = g73050; }
    
    public String getG73065() { return g73065; }
    public void setG73065(String g73065) { this.g73065 = g73065; }
    
    public int getG73180() { return g73180; }
    public void setG73180(int g73180) { this.g73180 = g73180; }
    
    public int getG73190() { return g73190; }
    public void setG73190(int g73190) { this.g73190 = g73190; }
    
    public int getG73200() { return g73200; }
    public void setG73200(int g73200) { this.g73200 = g73200; }
    
    public String getAktion() { return aktion; }
    public void setAktion(String aktion) { this.aktion = aktion; }
    
    public String getAuto() { return auto; }
    public void setAuto(String auto) { this.auto = auto; }
    
    public String getBereich() { return bereich; }
    public void setBereich(String bereich) { this.bereich = bereich; }
    
    public String getReturnValue() { return returnValue; }
    public void setReturnValue(String returnValue) { this.returnValue = returnValue; }
    
    public String getFgnr17() { return fgnr17; }
    public void setFgnr17(String fgnr17) { this.fgnr17 = fgnr17; }
    
    public String getWelche() { return welche; }
    public void setWelche(String welche) { this.welche = welche; }
    
    public String getMinimum() { return minimum; }
    public void setMinimum(String minimum) { this.minimum = minimum; }
    
    public String getSubgut() { return subgut; }
    public void setSubgut(String subgut) { this.subgut = subgut; }
    
    public String getGelb() { return gelb; }
    public void setGelb(String gelb) { this.gelb = gelb; }
    
    public String getFlag04() { return flag04; }
    public void setFlag04(String flag04) { this.flag04 = flag04; }
    
    public String getFlagx4() { return flagx4; }
    public void setFlagx4(String flagx4) { this.flagx4 = flagx4; }
    
    public String getF11txt() { return f11txt; }
    public void setF11txt(String f11txt) { this.f11txt = f11txt; }
    
    public String getF110() { return f110; }
    public void setF110(String f110) { this.f110 = f110; }
    
    public String getF111() { return f111; }
    public void setF111(String f111) { this.f111 = f111; }
    
    public LocalDate getAktdat() { return aktdat; }
    public void setAktdat(LocalDate aktdat) { this.aktdat = aktdat; }
    
    public LocalDate getVgldat1() { return vgldat1; }
    public void setVgldat1(LocalDate vgldat1) { this.vgldat1 = vgldat1; }
    
    public LocalDate getVgldat2() { return vgldat2; }
    public void setVgldat2(LocalDate vgldat2) { this.vgldat2 = vgldat2; }
    
    public int getTage() { return tage; }
    public void setTage(int tage) { this.tage = tage; }
    
    public boolean isEpsCheck() { return epsCheck; }
    public void setEpsCheck(boolean epsCheck) { this.epsCheck = epsCheck; }
    
    public BigDecimal getSubwert() { return subwert; }
    public void setSubwert(BigDecimal subwert) { this.subwert = subwert; }
    
    public BigDecimal getS3fCno() { return s3fCno; }
    public void setS3fCno(BigDecimal s3fCno) { this.s3fCno = s3fCno; }
    
    public BigDecimal getS3fFno() { return s3fFno; }
    public void setS3fFno(BigDecimal s3fFno) { this.s3fFno = s3fFno; }
    
    public BigDecimal getS3fDno() { return s3fDno; }
    public void setS3fDno(BigDecimal s3fDno) { this.s3fDno = s3fDno; }
    
    public BigDecimal getKey0011() { return key0011; }
    public void setKey0011(BigDecimal key0011) { this.key0011 = key0011; }
    
    public String getDatitt() { return datitt; }
    public void setDatitt(String datitt) { this.datitt = datitt; }
    
    public String getDatimm() { return datimm; }
    public void setDatimm(String datimm) { this.datimm = datimm; }
    
    public String getDatijj() { return datijj; }
    public void setDatijj(String datijj) { this.datijj = datijj; }
    
    public String getDatott() { return datott; }
    public void setDatott(String datott) { this.datott = datott; }
    
    public String getDatomm() { return datomm; }
    public void setDatomm(String datomm) { this.datomm = datomm; }
    
    public String getDatojj() { return datojj; }
    public void setDatojj(String datojj) { this.datojj = datojj; }
    
    public String getDatout() { return datout; }
    public void setDatout(String datout) { this.datout = datout; }
    
    public String getDatnij() { return datnij; }
    public void setDatnij(String datnij) { this.datnij = datnij; }
    
    public String getDatnim() { return datnim; }
    public void setDatnim(String datnim) { this.datnim = datnim; }
    
    public String getDatnit() { return datnit; }
    public void setDatnit(String datnit) { this.datnit = datnit; }
    
    public String getDatnoj() { return datnoj; }
    public void setDatnoj(String datnoj) { this.datnoj = datnoj; }
    
    public String getDatnom() { return datnom; }
    public void setDatnom(String datnom) { this.datnom = datnom; }
    
    public String getDatnot() { return datnot; }
    public void setDatnot(String datnot) { this.datnot = datnot; }
    
    public String getPunkt1() { return punkt1; }
    public void setPunkt1(String punkt1) { this.punkt1 = punkt1; }
    
    public String getPunkt2() { return punkt2; }
    public void setPunkt2(String punkt2) { this.punkt2 = punkt2; }
    
    public char getTnr71() { return tnr71; }
    public void setTnr71(char tnr71) { this.tnr71 = tnr71; }
    
    public char getTnr72() { return tnr72; }
    public void setTnr72(char tnr72) { this.tnr72 = tnr72; }
    
    public char getTnr73() { return tnr73; }
    public void setTnr73(char tnr73) { this.tnr73 = tnr73; }
    
    public char getTnr74() { return tnr74; }
    public void setTnr74(char tnr74) { this.tnr74 = tnr74; }
    
    public char getTnr75() { return tnr75; }
    public void setTnr75(char tnr75) { this.tnr75 = tnr75; }
    
    public char getTnr76() { return tnr76; }
    public void setTnr76(char tnr76) { this.tnr76 = tnr76; }
    
    public char getTnr77() { return tnr77; }
    public void setTnr77(char tnr77) { this.tnr77 = tnr77; }
    
    public String getRsd1ma() { return rsd1ma; }
    public void setRsd1ma(String rsd1ma) { this.rsd1ma = rsd1ma; }
    
    public String getRsd2ma() { return rsd2ma; }
    public void setRsd2ma(String rsd2ma) { this.rsd2ma = rsd2ma; }
    
    public String getRsd3ma() { return rsd3ma; }
    public void setRsd3ma(String rsd3ma) { this.rsd3ma = rsd3ma; }
    
    public String getRsd4ma() { return rsd4ma; }
    public void setRsd4ma(String rsd4ma) { this.rsd4ma = rsd4ma; }
    
    public int getZla() { return zla; }
    public void setZla(int zla) { this.zla = zla; }
    
    public String getReprint() { return reprint; }
    public void setReprint(String reprint) { this.reprint = reprint; }
    
    public int getZeile() { return zeile; }
    public void setZeile(int zeile) { this.zeile = zeile; }
    
    public int getFnr() { return fnr; }
    public void setFnr(int fnr) { this.fnr = fnr; }
    
    public String getG7106a() { return g7106a; }
    public void setG7106a(String g7106a) { this.g7106a = g7106a; }
    
    public String getG7111a() { return g7111a; }
    public void setG7111a(String g7111a) { this.g7111a = g7111a; }
    
    public String getSub140() { return sub140; }
    public void setSub140(String sub140) { this.sub140 = sub140; }
    
    public BigDecimal getRec1() { return rec1; }
    public void setRec1(BigDecimal rec1) { this.rec1 = rec1; }
    
    public BigDecimal getPag1() { return pag1; }
    public void setPag1(BigDecimal pag1) { this.pag1 = pag1; }
    
    public String getG71010() { return g71010; }
    public void setG71010(String g71010) { this.g71010 = g71010; }
    
    public String getG71020() { return g71020; }
    public void setG71020(String g71020) { this.g71020 = g71020; }
    
    public String getG71030() { return g71030; }
    public void setG71030(String g71030) { this.g71030 = g71030; }
    
    public String getG71040() { return g71040; }
    public void setG71040(String g71040) { this.g71040 = g71040; }
    
    public String getG71070() { return g71070; }
    public void setG71070(String g71070) { this.g71070 = g71070; }
    
    public String getG7107a() { return g7107a; }
    public void setG7107a(String g7107a) { this.g7107a = g7107a; }
    
    public int getG7110a() { return g7110a; }
    public void setG7110a(int g7110a) { this.g7110a = g7110a; }
    
    public String getG7112a() { return g7112a; }
    public void setG7112a(String g7112a) { this.g7112a = g7112a; }
    
    public String getG7113a() { return g7113a; }
    public void setG7113a(String g7113a) { this.g7113a = g7113a; }
    
    public int getG7109a() { return g7109a; }
    public void setG7109a(int g7109a) { this.g7109a = g7109a; }

    // Indicator methods
    public boolean isIndicator(int ind) {
        return indicators.getOrDefault(ind, false);
    }
    
    public void setIndicator(int ind, boolean value) {
        indicators.put(ind, value);
    }
    
    public void clearIndicator(int ind) {
        indicators.put(ind, false);
    }
    
    public void clearIndicators(int... inds) {
        for (int ind : inds) {
            indicators.put(ind, false);
        }
    }
    
    public void setIndicators(int... inds) {
        for (int ind : inds) {
            indicators.put(ind, true);
        }
    }

    // Display/Write methods (would interact with UI layer)
    public void writeRecord(String recordName) {
        // Implementation would interact with UI layer
    }
    
    public void displayRecord(String recordName) {
        // Implementation would interact with UI layer
    }
    
    public void writeSubfileRecord(String subfileName, SubfileRecord record) {
        // Implementation would interact with UI layer
    }
    
    public void updateSubfileRecord(String subfileName, SubfileRecord record) {
        // Implementation would interact with UI layer
    }
    
    public List<SubfileRecord> readChangedSubfile(String subfileName) {
        // Implementation would interact with UI layer
        return new ArrayList<>();
    }
}

/**
 * Subfile record structure
 */
class SubfileRecord {
    private String mark1, mark4;
    private String sub060, sub065, sub070, sub080, sub100, sub110, sub140;
    private int sub180, sub190, sub200, sub240;
    private String sub280, substa;
    
    // Getters and setters
    public String getMark1() { return mark1; }
    public void setMark1(String mark1) { this.mark1 = mark1; }
    
    public String getMark4() { return mark4; }
    public void setMark4(String mark4) { this.mark4 = mark4; }
    
    public String getSub060() { return sub060; }
    public void setSub060(String sub060) { this.sub060 = sub060; }
    
    public String getSub065() { return sub065; }
    public void setSub065(String sub065) { this.sub065 = sub065; }
    
    public String getSub070() { return sub070; }
    public void setSub070(String sub070) { this.sub070 = sub070; }
    
    public String getSub080() { return sub080; }
    public void setSub080(String sub080) { this.sub080 = sub080; }
    
    public String getSub100() { return sub100; }
    public void setSub100(String sub100) { this.sub100 = sub100; }
    
    public String getSub110() { return sub110; }
    public void setSub110(String sub110) { this.sub110 = sub110; }
    
    public String getSub140() { return sub140; }
    public void setSub140(String sub140) { this.sub140 = sub140; }
    
    public int getSub180() { return sub180; }
    public void setSub180(int sub180) { this.sub180 = sub180; }
    
    public int getSub190() { return sub190; }
    public void setSub190(int sub190) { this.sub190 = sub190; }
    
    public int getSub200() { return sub200; }
    public void setSub200(int sub200) { this.sub200 = sub200; }
    
    public int getSub240() { return sub240; }
    public void setSub240(int sub240) { this.sub240 = sub240; }
    
    public String getSub280() { return sub280; }
    public void setSub280(String sub280) { this.sub280 = sub280; }
    
    public String getSubsta() { return substa; }
    public void setSubsta(String substa) { this.substa = substa; }
}

/**
 * Key classes for database access
 */
class HSG73Key {
    private String kzl, cnr, fnr;
    
    public HSG73Key(String kzl, String cnr, String fnr) {
        this.kzl = kzl;
        this.cnr = cnr;
        this.fnr = fnr;
    }
    
    // Getters
    public String getKzl() { return kzl; }
    public String getCnr() { return cnr; }
    public String getFnr() { return fnr; }
}

class HSGPSKey {
    private String kzl, cnr, fnr;
    
    public HSGPSKey(String kzl, String cnr) {
        this.kzl = kzl;
        this.cnr = cnr;
    }
    
    public HSGPSKey(String kzl, String cnr, String fnr) {
        this.kzl = kzl;
        this.cnr = cnr;
        this.fnr = fnr;
    }
    
    // Getters
    public String getKzl() { return kzl; }
    public String getCnr() { return cnr; }
    public String getFnr() { return fnr; }
}

class S3F004Key {
    private BigDecimal cno, fno, dno;
    
    public S3F004Key(BigDecimal cno, BigDecimal fno, BigDecimal dno) {
        this.cno = cno;
        this.fno = fno;
        this.dno = dno;
    }
    
    // Getters
    public BigDecimal getCno() { return cno; }
    public BigDecimal getFno() { return fno; }
    public BigDecimal getDno() { return dno; }
}

// Placeholder entity classes (would be fully defined with all columns from DB contracts)
class HSG71Entity {}
class HSG73Entity {
    private String g73060, g73065, g73070, g73080, g73100, g73110, g73140, g73280;
    private int g73180, g73190, g73200, g73240, g73290;
    
    public String getG73060() { return g73060; }
    public String getG73065() { return g73065; }
    public String getG73070() { return g73070; }
    public String getG73080() { return g73080; }
    public String getG73100() { return g73100; }
    public String getG73110() { return g73110; }
    public String getG73140() { return g73140; }
    public String getG73280() { return g73280; }
    public int getG73180() { return g73180; }
    public int getG73190() { return g73190; }
    public int getG73200() { return g73200; }
    public int getG73240() { return g73240; }
    public int getG73290() { return g73290; }
    public void setG73290(int g73290) { this.g73290 = g73290; }
}
class HSGPSEntity {
    private String gps020, gps040;
    private int gps030, gps120;
    private BigDecimal gps070;
    
    public String getGps020() { return gps020; }
    public String getGps040() { return gps040; }
    public int getGps030() { return gps030; }
    public int getGps120() { return gps120; }
    public void setGps120(int gps120) { this.gps120 = gps120; }
    public BigDecimal getGps070() { return gps070; }
}
class S3F004Entity {}
class S3F103Entity {}
class S3F104Entity {}
class S3F105Entity {}
class S3F106Entity {}

// Placeholder repository interfaces
interface HSG71Repository {
    Optional<HSG71Entity> findByKey(HSG73Key key);
    List<HSG71Entity> findByClaimNumber(String cn);
}
interface HSG73Repository {
    List<HSG73Entity> findByKey(HSG73Key key);
    Optional<HSG73Entity> findByKey(HSG73Key key);
    void save(HSG73Entity entity);
    void delete(HSG73Entity entity);
}
interface HSGPSRepository {
    List<HSGPSEntity> findByKey(HSGPSKey key);
    void save(HSGPSEntity entity);
}
interface AUFWSKRepository {}
interface FARSTLRepository {}
interface S3F004Repository {
    Optional<S3F004Entity> findByKey(S3F004Key key);
}
interface S3F103Repository {
    List<S3F103Entity> findByKey(Object key);
}
interface S3F104Repository {
    List<S3F104Entity> findByKey(Object key);
}
interface S3F105Repository {
    List<S3F105Entity> findByKey(Object key);
}
interface S3F106Repository {
    List<S3F106Entity> findByKey(Object key);
}

// Placeholder service interfaces
interface ClaimService {
    void loadClaim(ClaimContext ctx);
    void callHS1215(String art, String p1, String p2, String p3, String p4, String p5, String p6, String p7, String p8);
    void callHS1217(String p1, String p2, String p3, String p4, String p5, String p6, String p7, String p8, String p9);
    void callHS1228(String p1, String p2, String p3, String p4, String p5, String p6, String p7);
    void printClaim(String p1, String p2, String p3, String p4, String p5);
    void sendClaim(String p1, String p2, String p3, String p4);
    void callHS1212C2(String p1, String p2, String p3);
    void callHs1212C3(String gutart, String subgut);
    void callHs1295(String gutart, String subgut, String reprint);
    void displaySplf();
    void deleteSplf();
    void callHS0069C(String fgnr17);
    void getDescription(ClaimContext ctx);
}
interface TextService {
    void loadText(ClaimContext ctx, String type);
    void saveText(ClaimContext ctx);
    void loadOrderTextSfl(ClaimContext ctx);
}
interface ValidationService {
    boolean isWarrScope(String scope);
    String getScope(String field);
}
interface EpsService {}
interface CampaignService {
    void callHS1213(String p1, String p2, String p3, String p4, String p5, String p6, String p7, String p8, String p9, String p10, String p11);
}

