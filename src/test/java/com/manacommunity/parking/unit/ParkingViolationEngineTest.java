package com.manacommunity.parking.unit;

import com.manacommunity.parking.engine.ParkingViolationEngine;
import com.manacommunity.parking.enums.ViolationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ParkingViolationEngine Unit Tests")
class ParkingViolationEngineTest {

    private ParkingViolationEngine engine;

    @BeforeEach
    void setUp() {
        engine = new ParkingViolationEngine();
    }

    @Test
    @DisplayName("calculateFine: calculates tiered penalties on repeated violations")
    void calculateFine_tiered() {
        BigDecimal f1 = engine.calculateFine(ViolationType.UNAUTHORIZED_PARKING, 0);
        BigDecimal f2 = engine.calculateFine(ViolationType.UNAUTHORIZED_PARKING, 1);
        BigDecimal f3 = engine.calculateFine(ViolationType.UNAUTHORIZED_PARKING, 2);

        assertThat(f1).isEqualByComparingTo("500.00");
        assertThat(f2).isEqualByComparingTo("750.00");
        assertThat(f3).isEqualByComparingTo("1000.00");
    }
}
