package com.scania.warranty.service;

import org.springframework.stereotype.Service;

@Service
public class ClaimActionService {

    public void executeClaimAction(
            String companyCode,
            String claimNumber,
            String claimSequence,
            String invoiceNumber,
            String invoiceSequence,
            String claimLineNumber,
            String failureCode,
            String autoFlag) {
        
        System.out.println("Executing claim action HS1217:");
        System.out.println("  Company Code: " + companyCode);
        System.out.println("  Claim Number: " + claimNumber);
        System.out.println("  Claim Sequence: " + claimSequence);
        System.out.println("  Invoice Number: " + invoiceNumber);
        System.out.println("  Invoice Sequence: " + invoiceSequence);
        System.out.println("  Claim Line Number: " + claimLineNumber);
        System.out.println("  Failure Code: " + failureCode);
        System.out.println("  Auto Flag: " + autoFlag);
    }
}