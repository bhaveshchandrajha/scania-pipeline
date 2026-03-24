package com.scania.warranty.repository;

import com.scania.warranty.domain.Claim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, String> {

    Optional<Claim> findByClaimNumber(String claimNumber);

    List<Claim> findByDealerIdAndInvoiceNumberAndInvoiceDate(
        String dealerId, String invoiceNumber, String invoiceDate);

    List<Claim> findByDealerIdAndOrderNumber(String dealerId, String orderNumber);

    List<Claim> findByChassisNumber(String chassisNumber);

    @Query("SELECT c FROM Claim c WHERE c.dealerId = :dealerId " +
           "AND (:invoiceNumber = '' OR c.invoiceNumber = :invoiceNumber) " +
           "AND (:invoiceDate = '' OR c.invoiceDate = :invoiceDate) " +
           "AND (:orderNumber = '' OR c.orderNumber = :orderNumber) " +
           "AND (:chassisNumber = '' OR c.chassisNumber = :chassisNumber) " +
           "AND (:registrationNumber = '' OR c.registrationNumber = :registrationNumber)")
    List<Claim> searchClaims(
        @Param("dealerId") String dealerId,
        @Param("invoiceNumber") String invoiceNumber,
        @Param("invoiceDate") String invoiceDate,
        @Param("orderNumber") String orderNumber,
        @Param("chassisNumber") String chassisNumber,
        @Param("registrationNumber") String registrationNumber);

    @Query("SELECT c FROM Claim c JOIN c.errors e WHERE c.dealerId = :dealerId " +
           "AND c.claimNumber = :claimNumber")
    Optional<Claim> findClaimWithErrors(
        @Param("dealerId") String dealerId,
        @Param("claimNumber") String claimNumber);

    @Query("SELECT c FROM Claim c WHERE c.dealerId = :dealerId " +
           "AND c.sdeStatusCode >= :statusFrom AND c.sdeStatusCode <= :statusTo")
    List<Claim> findByDealerIdAndStatusRange(
        @Param("dealerId") String dealerId,
        @Param("statusFrom") Integer statusFrom,
        @Param("statusTo") Integer statusTo);

    @Query("SELECT MAX(c.claimNumber) FROM Claim c WHERE c.dealerId = :dealerId " +
           "AND c.invoiceNumber = :invoiceNumber AND c.invoiceDate = :invoiceDate " +
           "AND c.orderNumber = :orderNumber")
    Optional<String> findMaxClaimNumber(
        @Param("dealerId") String dealerId,
        @Param("invoiceNumber") String invoiceNumber,
        @Param("invoiceDate") String invoiceDate,
        @Param("orderNumber") String orderNumber);
}