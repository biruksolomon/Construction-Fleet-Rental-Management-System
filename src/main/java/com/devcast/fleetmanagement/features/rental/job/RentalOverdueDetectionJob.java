package com.devcast.fleetmanagement.features.rental.job;

import com.devcast.fleetmanagement.features.rental.model.RentalContract;
import com.devcast.fleetmanagement.features.rental.repository.RentalContractRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Rental Overdue Detection Job
 * Scheduled task that periodically checks for overdue rentals and updates their status
 * Runs every hour to detect and mark rentals that have exceeded their end date
 */
@Component
@Slf4j
@RequiredArgsConstructor
@Transactional
public class RentalOverdueDetectionJob {

    private final RentalContractRepository rentalContractRepository;

    /**
     * Detect and mark overdue rentals
     * Runs every hour at the start of the hour
     */
    @Scheduled(cron = "0 0 * * * *")
    public void detectOverdueRentals() {
        log.info("Starting overdue rental detection job");

        try {
            // Find all active rentals
            List<RentalContract> activeRentals = rentalContractRepository.findByStatus(
                    RentalContract.RentalStatus.ACTIVE
            );

            LocalDate today = LocalDate.now();
            int overdueCount = 0;

            for (RentalContract rental : activeRentals) {
                // Check if end date has passed
                if (today.isAfter(rental.getEndDate())) {
                    log.warn("Marking rental {} as overdue (end date: {})", rental.getId(), rental.getEndDate());
                    rental.markOverdue();
                    rentalContractRepository.save(rental);
                    overdueCount++;
                }
            }

            log.info("Overdue rental detection completed. Found {} overdue rentals", overdueCount);
        } catch (Exception e) {
            log.error("Error during overdue rental detection", e);
        }
    }

    /**
     * Clean up old deleted rentals
     * Runs daily at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void cleanupDeletedRentals() {
        log.info("Starting deleted rental cleanup job");

        try {
            // Delete soft-deleted rentals older than 90 days
            rentalContractRepository.deleteDeletedRentalsOlderThan(90);
            log.info("Deleted rental cleanup completed");
        } catch (Exception e) {
            log.error("Error during deleted rental cleanup", e);
        }
    }
}
