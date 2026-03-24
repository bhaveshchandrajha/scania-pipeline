/**
 * Application service implementing warranty claim business logic.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.service;

import com.scania.warranty.domain.*;
import com.scania.warranty.dto.MinimumClaimBookingDto;
import com.scania.warranty.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ClaimManagementService {

    private final ClaimRepository claimRepository;
    private final ClaimErrorRepository claimErrorRepository;
    private final ClaimReleaseRequestRepository claimReleaseRequestRepository;
    private final InvoiceRepository invoiceRepository;

    public ClaimManagementService(ClaimRepository claimRepository,
                                   ClaimErrorRepository claimErrorRepository,
                                   ClaimReleaseRequestRepository claimReleaseRequestRepository,
                                   InvoiceRepository invoiceRepository) {
        this.claimRepository = claimRepository; // @rpg-trace: n553
        this.claimErrorRepository = claimErrorRepository; // @rpg-trace: n592
        this.claimReleaseRequestRepository = claimReleaseRequestRepository; // @rpg-trace: n1706
        this.invoiceRepository = invoiceRepository; // @rpg-trace: n975
    }

    @Transactional
    public void deleteClaims(String companyCode, List<String> claimNrs) { // @rpg-trace: n557
        for (String claimNr : claimNrs) { // @rpg-trace: n583
            Optional<Claim> claimOpt = claimRepository.findByG71000AndG71050(companyCode, claimNr); // @rpg-trace: n586
            if (claimOpt.isPresent()) { // @rpg-trace: n587
                Claim claim = claimOpt.get(); // @rpg-trace: n587
                claim.setG71170(new BigDecimal(ClaimStatus.EXCLUDED.getCode())); // @rpg-trace: n589
                claimRepository.save(claim); // @rpg-trace: n589

                claimErrorRepository.deleteByCompanyAndClaim(companyCode, claimNr); // @rpg-trace: n592
            }
        }
    }

    @Transactional
    public boolean changeClaimStatus(String companyCode, String claimNr) { // @rpg-trace: n653
        Optional<Claim> claimOpt = claimRepository.findByG71000AndG71050(companyCode, claimNr); // @rpg-trace: n653
        if (claimOpt.isPresent()) { // @rpg-trace: n654
            Claim claim = claimOpt.get(); // @rpg-trace: n654
            int currentStatus = claim.getG71170() != null ? claim.getG71170().intValue() : 0; // @rpg-trace: n655
            if (currentStatus == 2) { // @rpg-trace: n657
                claim.setG71170(new BigDecimal(3)); // @rpg-trace: n658
                claimRepository.save(claim); // @rpg-trace: n658
                return true; // @rpg-trace: n658
            }
        }
        return false; // @rpg-trace: n663
    }

    @Transactional
    public boolean bookMinimumClaim(MinimumClaimBookingDto request) { // @rpg-trace: n1663
        Optional<Claim> claimOpt = claimRepository.findByG71000AndG71050(request.companyCode(), request.claimNr()); // @rpg-trace: n1654
        if (claimOpt.isEmpty()) { // @rpg-trace: n1664
            return false; // @rpg-trace: n1664
        }
        Claim claim = claimOpt.get(); // @rpg-trace: n1665
        int statusCode = claim.getG71170() != null ? claim.getG71170().intValue() : 0; // @rpg-trace: n1665

        if (statusCode != ClaimStatus.MINIMUM.getCode()) { // @rpg-trace: n1667
            return false; // @rpg-trace: n1667
        }

        claim.setG71170(new BigDecimal(ClaimStatus.APPROVED.getCode())); // @rpg-trace: n1670
        claimRepository.save(claim); // @rpg-trace: n1671

        return true; // @rpg-trace: n1668
    }

    @Transactional
    public boolean createReleaseRequest(String companyCode, String invoiceNr, String invoiceDateIso) { // @rpg-trace: n1706
        if (invoiceDateIso == null || invoiceDateIso.isBlank() || invoiceNr == null || invoiceNr.isBlank()) { // @rpg-trace: n1710
            return false; // @rpg-trace: n1710
        }

        Optional<ClaimReleaseRequest> existing = claimReleaseRequestRepository
            .findByG70KzlAndG70RnrAndG70Rdat(companyCode, invoiceNr, invoiceDateIso); // @rpg-trace: n1707

        if (existing.isPresent()) { // @rpg-trace: n1708
            return false; // already exists // @rpg-trace: n1708
        }

        ClaimReleaseRequest releaseRequest = new ClaimReleaseRequest(); // @rpg-trace: n1711
        releaseRequest.setG70Kzl(companyCode); // @rpg-trace: n1711
        releaseRequest.setG70Rnr(invoiceNr); // @rpg-trace: n1712
        releaseRequest.setG70Rdat(invoiceDateIso); // @rpg-trace: n1713
        releaseRequest.setG70Fgnr(""); // @rpg-trace: n1714
        releaseRequest.setG70Dat(""); // @rpg-trace: n1715

        Optional<Invoice> invoiceOpt = invoiceRepository.findByInvoiceKey(companyCode, invoiceNr, invoiceDateIso, "", ""); // @rpg-trace: n1716
        if (invoiceOpt.isPresent()) { // @rpg-trace: n1717
            Invoice inv = invoiceOpt.get(); // @rpg-trace: n1718
            releaseRequest.setG70Fgnr(inv.getAhk510()); // @rpg-trace: n1719
            releaseRequest.setG70Dat(inv.getAhk620()); // @rpg-trace: n1720
        }

        releaseRequest.setG70Status(""); // @rpg-trace: n1722
        releaseRequest.setG70Cusno(BigDecimal.ZERO); // @rpg-trace: n1722
        releaseRequest.setG70Clmno(BigDecimal.ZERO); // @rpg-trace: n1722
        releaseRequest.setG70Clmfl(""); // @rpg-trace: n1722
        claimReleaseRequestRepository.save(releaseRequest); // @rpg-trace: n1723

        return true; // @rpg-trace: n1723
    }
}