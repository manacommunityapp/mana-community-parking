package com.manacommunity.parking.unit;

import com.manacommunity.parking.engine.EvBillingEngine;
import com.manacommunity.parking.model.EvChargingRate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("EvBillingEngine Unit Tests")
class EvBillingEngineTest {

    private EvBillingEngine engine;
    private EvChargingRate rateConfig;

    @BeforeEach
    void setUp() {
        engine = new EvBillingEngine();
        rateConfig = EvChargingRate.builder()
                .unitRatePerKwh(new BigDecimal("10.00"))
                .peakRatePerKwh(new BigDecimal("15.00"))
                .peakStartHour(18)
                .peakEndHour(22)
                .minimumBillAmount(new BigDecimal("30.00"))
                .build();
    }

    @Test
    @DisplayName("calculateKwh: computes units consumed")
    void calculateKwh() {
        assertThat(engine.calculateKwh(100.0, 125.5)).isEqualTo(25.5);
    }

    @Test
    @DisplayName("calculateCost: standard rate")
    void calculateCost_standardRate() {
        LocalDateTime start = LocalDateTime.of(2026, 10, 1, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 10, 1, 12, 0);
        BigDecimal cost = engine.calculateCost(10.0, start, end, rateConfig);
        assertThat(cost).isEqualByComparingTo("100.00");
    }
}
