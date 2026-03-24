package com.scania.warranty.web;

import com.scania.warranty.domain.Invoice;
import com.scania.warranty.dto.InvoiceListItemDto;
import com.scania.warranty.repository.InvoiceRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for listing invoices available for claim creation.
 */
@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final InvoiceRepository invoiceRepository;

    public InvoiceController(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    @GetMapping
    public ResponseEntity<List<InvoiceListItemDto>> listInvoices(
            @RequestParam(defaultValue = "001") String companyCode) {
        List<Invoice> invoices = invoiceRepository.findByCompany(companyCode);
        List<InvoiceListItemDto> dtos = invoices.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    private InvoiceListItemDto toDto(Invoice inv) {
        return new InvoiceListItemDto(
                inv.getAhk000(),
                inv.getAhk010(),
                inv.getAhk080(),
                inv.getAhk040(),
                inv.getAhk050(),
                inv.getAhk250() != null ? inv.getAhk250().trim() : ""
        );
    }
}