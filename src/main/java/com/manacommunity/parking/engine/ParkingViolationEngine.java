package com.manacommunity.parking.engine;

import com.manacommunity.parking.enums.ViolationType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ParkingViolationEngine {

    /**
     * Determines the tiered penalty fine based on violation type and occurrence count.
     */
    public BigDecimal calculateFine(ViolationType type, int priorViolations) {
        BigDecimal baseFine = switch (type) {
            case UNAUTHORIZED_PARKING -> new BigDecimal("500.00");
            case OVERSTAY -> new BigDecimal("200.00");
            case WRONG_SLOT -> new BigDecimal("300.00");
            case BLOCKING_DRIVEWAY -> new BigDecimal("1000.00");
            case SPEEDING, RECKLESS_DRIVING -> new BigDecimal("1500.00");
            case NO_PARKING_ZONE -> new BigDecimal("500.00");
        };

        if (priorViolations == 0) {
            return baseFine;
        } else if (priorViolations == 1) {
            return baseFine.multiply(new BigDecimal("1.50"));
        } else {
            return baseFine.multiply(new BigDecimal("2.00"));
        }
    }
}
