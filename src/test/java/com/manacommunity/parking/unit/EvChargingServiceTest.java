package com.manacommunity.parking.unit;

import com.manacommunity.parking.dto.EvTelemetryRequest;
import com.manacommunity.parking.dto.StartChargingRequest;
import com.manacommunity.parking.dto.StopChargingRequest;
import com.manacommunity.parking.dto.EvChargingSessionResponse;
import com.manacommunity.parking.engine.EvBillingEngine;
import com.manacommunity.parking.enums.ChargerStatus;
import com.manacommunity.parking.model.EvCharger;
import com.manacommunity.parking.model.EvChargingSession;
import com.manacommunity.parking.repository.EvChargerRepository;
import com.manacommunity.parking.repository.EvChargingRateRepository;
import com.manacommunity.parking.repository.EvChargingSessionRepository;
import com.manacommunity.parking.repository.EvTelemetryHeartbeatRepository;
import com.manacommunity.parking.service.EvChargingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EvChargingService Unit Tests")
class EvChargingServiceTest {

    @Mock private EvChargerRepository chargerRepository;
    @Mock private EvChargingRateRepository rateRepository;
    @Mock private EvChargingSessionRepository sessionRepository;
    @Mock private EvTelemetryHeartbeatRepository heartbeatRepository;
    @Mock private EvBillingEngine billingEngine;

    @InjectMocks
    private EvChargingService service;

    private EvCharger testCharger;

    @BeforeEach
    void setUp() {
        testCharger = EvCharger.builder()
                .id(1L)
                .deviceId("CHARGER-01")
                .communityId(10L)
                .status(ChargerStatus.AVAILABLE)
                .latestMeterKwh(100.0)
                .build();
    }

    @Test
    @DisplayName("startSession: starts session successfully")
    void startSession_success() {
        when(chargerRepository.findById(1L)).thenReturn(Optional.of(testCharger));
        when(sessionRepository.save(any())).thenAnswer(inv -> {
            EvChargingSession s = inv.getArgument(0);
            s.setId(100L);
            return s;
        });

        StartChargingRequest req = new StartChargingRequest(1L, null, 100.0);
        EvChargingSessionResponse res = service.startSession(55L, 10L, req);

        assertThat(res.id()).isEqualTo(100L);
        assertThat(testCharger.getStatus()).isEqualTo(ChargerStatus.CHARGING);
        verify(chargerRepository).save(testCharger);
    }

    @Test
    @DisplayName("stopSession: completes session and updates charger to AVAILABLE")
    void stopSession_success() {
        EvChargingSession session = EvChargingSession.builder()
                .id(100L)
                .communityId(10L)
                .charger(testCharger)
                .residentId(55L)
                .startedAt(LocalDateTime.now().minusHours(2))
                .startMeterKwh(100.0)
                .status("ACTIVE")
                .build();

        when(sessionRepository.findById(100L)).thenReturn(Optional.of(session));
        when(billingEngine.calculateKwh(100.0, 120.0)).thenReturn(20.0);
        when(billingEngine.calculateCost(eq(20.0), any(), any(), any())).thenReturn(new BigDecimal("200.00"));
        when(sessionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        StopChargingRequest req = new StopChargingRequest(120.0, "USER_STOP");
        EvChargingSessionResponse res = service.stopSession(100L, req);

        assertThat(res.status()).isEqualTo("COMPLETED");
        assertThat(res.totalCost()).isEqualByComparingTo("200.00");
        assertThat(testCharger.getStatus()).isEqualTo(ChargerStatus.AVAILABLE);
    }
}
