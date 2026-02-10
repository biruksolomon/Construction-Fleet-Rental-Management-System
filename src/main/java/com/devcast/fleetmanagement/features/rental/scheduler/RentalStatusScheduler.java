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
 * PHASE 6: Automated Rental Status Management
 *
 * Scheduled job to automatically update rental contract statuses based on dates:
 * - PENDING → ACTIVE when start date is reached
 * - ACTIVE → OVERDUE when end date is exceeded
 *
 * Runs every hour to ensure timely status updates
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RentalStatusScheduler {

    private final RentalContractRepository rentalContractRepository;

    /**
     * Activate pending rentals that have reached their start date
     * Runs every hour at minute 0
     */
    @Scheduled(cron = "0 0 * * * *") // Every hour
    @Transactional
    public void activatePendingRentals() {
        LocalDate today = LocalDate.now();

        log.info("Running scheduled task: activatePendingRentals for date {}", today);

        // Find PENDING rentals where start date is today or in the past
        List<RentalContract> pendingRentals = rentalContractRepository
                .findByStatusAndStartDateLessThanEqual(
                        RentalContract.RentalStatus.PENDING,
                        today
                );

        int activated = 0;
        for (RentalContract rental : pendingRentals) {
            try {
                rental.transitionTo(RentalContract.RentalStatus.ACTIVE);
                rentalContractRepository.save(rental);
                activated++;

                log.info("Auto-activated rental contract: {} (Company: {})",
                        rental.getContractNumber(),
                        rental.getCompany().getId());
            } catch (Exception e) {
                log.error("Failed to activate rental contract {}: {}",
                        rental.getContractNumber(), e.getMessage());
            }
        }

        log.info("Completed activatePendingRentals: {} rentals activated", activated);
    }

    /**
     * Mark active rentals as overdue when end date is exceeded
     * Runs every hour at minute 15
     */
    @Scheduled(cron = "0 15 * * * *") // Every hour at :15
    @Transactional
    public void markOverdueRentals() {
        LocalDate today = LocalDate.now();

        log.info("Running scheduled task: markOverdueRentals for date {}", today);

        // Find ACTIVE rentals where end date has passed
        List<RentalContract> activeRentals = rentalContractRepository
                .findByStatusAndEndDateLessThan(
                        RentalContract.RentalStatus.ACTIVE,
                        today
                );

        int marked = 0;
        for (RentalContract rental : activeRentals) {
            try {
                rental.transitionTo(RentalContract.RentalStatus.OVERDUE);
                rentalContractRepository.save(rental);
                marked++;

                log.warn("Auto-marked rental contract as OVERDUE: {} (Company: {})",
                        rental.getContractNumber(),
                        rental.getCompany().getId());
            } catch (Exception e) {
                log.error("Failed to mark rental contract {} as overdue: {}",
                        rental.getContractNumber(), e.getMessage());
            }
        }

        log.info("Completed markOverdueRentals: {} rentals marked as overdue", marked);
    }

    /**
     * Generate daily summary report of rental status changes
     * Runs every day at 8:00 AM
     */
    @Scheduled(cron = "0 0 8 * * *") // Daily at 8:00 AM
    @Transactional(readOnly = true)
    public void generateDailyStatusReport() {
        log.info("Generating daily rental status report");

        long activeCount = rentalContractRepository.countByStatus(RentalContract.RentalStatus.ACTIVE);
        long pendingCount = rentalContractRepository.countByStatus(RentalContract.RentalStatus.PENDING);
        long overdueCount = rentalContractRepository.countByStatus(RentalContract.RentalStatus.OVERDUE);

        log.info("Daily Status Report - ACTIVE: {}, PENDING: {}, OVERDUE: {}",
                activeCount, pendingCount, overdueCount);
    }
}
