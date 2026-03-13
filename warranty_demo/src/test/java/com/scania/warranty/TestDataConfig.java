package com.scania.warranty;

import com.scania.warranty.domain.Invoice;
import com.scania.warranty.repository.InvoiceRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Loads seed test data when running with the "test" profile.
 * Uses ApplicationRunner so it runs after context init, with transaction support.
 */
@Component
@Profile("test")
@Order(Integer.MIN_VALUE)
public class TestDataConfig implements ApplicationRunner {

    private final InvoiceRepository invoiceRepository;
    private final TestDataFactory testDataFactory;

    public TestDataConfig(InvoiceRepository invoiceRepository, TestDataFactory testDataFactory) {
        this.invoiceRepository = invoiceRepository;
        this.testDataFactory = testDataFactory;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        invoiceRepository.deleteByPakzAndRnrAndRdat(
                TestDataFactory.SEED_PAKZ, TestDataFactory.SEED_RNR, TestDataFactory.SEED_RDAT);
        Invoice inv = testDataFactory.createSeedInvoice();
        invoiceRepository.save(inv);
    }
}
