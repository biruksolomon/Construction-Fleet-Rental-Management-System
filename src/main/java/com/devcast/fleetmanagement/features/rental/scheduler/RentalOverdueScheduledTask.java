package com.devcast.fleetmanagement.features.rental.scheduler;

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
 * Rental Overdue Detection Scheduled Task
 *
 * PHASE 9: Scheduled Job - Overdue Detection
 * Automatically marks rentals as OVERDUE when endDate passes
 * Runs hourly to detect and transition expired rentals
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class RentalOverdueScheduledTask {

    private final RentalContractRepository rentalContractRepository;

    /**
     * Detect and mark overdue rentals
     * Finds all ACTIVE rentals with end date in the past and transitions them to OVERDUE
     *
     * Scheduled to run: Every hour at the top of the hour (0 0 * * * *)
     * Cron expression: minute hour day month dayOfWeek
     */
    @Scheduled(cron = "0 0 * * * *") // Every hour
    @Transactional
    public void detectAndMarkOverdueRentals() {
        log.info("Starting overdue rental detection scheduled task");

        try {
            LocalDate today = LocalDate.now();

            // Find all ACTIVE rentals with end date in the past
            List<RentalContract> activeRentals = rentalContractRepository
                    .findByStatusAndEndDateLessThan(RentalContract.RentalStatus.ACTIVE, today);

            if (activeRentals.isEmpty()) {
                log.debug("No overdue rentals detected");
                return;
            }

            int count = 0;
            for (RentalContract rental : activeRentals) {
                try {
                    // Transition from ACTIVE to OVERDUE
                    rental.transitionTo(RentalContract.RentalStatus.OVERDUE);
                    rentalContractRepository.save(rental);
                    count++;

                    log.info("Rental contract {} marked as OVERDUE (EndDate: {})",
                            rental.getContractNumber(), rental.getEndDate());

                } catch (Exception e) {
                    log.error("Failed to mark rental {} as OVERDUE: {}",
                            rental.getId(), e.getMessage());
                }
            }

            log.info("Overdue rental detection completed. {} rentals marked as OVERDUE", count);

        } catch (Exception e) {
            log.error("Error during overdue rental detection: {}", e.getMessage(), e);
        }
    }

    /**
     * Detect and mark rentals past completion deadline (alternative task)
     * Can be extended for additional business logic like sending notifications
     *
     * Scheduled to run: Twice daily (6 AM and 6 PM)
     */
    @Scheduled(cron = "0 0 6,18 * * *") // 6 AM and 6 PM
    @Transactional
    public void checkAndNotifyOverdueRentals() {
        log.debug("Running supplementary overdue rental check");

        try {
            LocalDate today = LocalDate.now();

            // Find overdue rentals still open (not yet completed)
            List<RentalContract> overdueRentals = rentalContractRepository
                    .findByStatusAndEndDateLessThan(RentalContract.RentalStatus.OVERDUE, today);

            if (!overdueRentals.isEmpty()) {
                log.warn("Found {} OVERDUE rentals that still need attention", overdueRentals.size());
                // TODO: Send notifications to company/client about overdue rentals
            }

        } catch (Exception e) {
            log.error("Error during overdue rental notification check: {}", e.getMessage(), e);
        }
    }
}
