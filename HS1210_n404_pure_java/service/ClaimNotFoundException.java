package com.scania.warranty.service;

public class ClaimNotFoundException extends RuntimeException {
    
    public ClaimNotFoundException(String pakz, String claimnr) {
        super(String.format("Claim not found: pakz=%s, claimnr=%s", pakz, claimnr));
    }
}