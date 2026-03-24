package com.scania.warranty.repository;

import com.scania.warranty.domain.Claim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, String> {

    @Query("SELECT c FROM Claim c WHERE c.companyCode = :companyCode AND c.invoiceNumber = :invoiceNumber AND c.invoiceDate = :invoiceDate")
    Optional<Claim> findByCompanyCodeAndInvoiceNumberAndInvoiceDate(
            @Param("companyCode") String companyCode,
            @Param("invoiceNumber") String invoiceNumber,
            @Param("invoiceDate") String invoiceDate);

    @Modifying
    @Query("UPDATE Claim c SET c.statusCodeSde = :statusCode WHERE c.companyCode = :companyCode AND c.invoiceNumber = :invoiceNumber AND c.invoiceDate = :invoiceDate")
    int updateStatusCode(
            @Param("companyCode") String companyCode,
            @Param("invoiceNumber") String invoiceNumber,
            @Param("invoiceDate") String invoiceDate,
            @Param("statusCode") Integer statusCode);
}