package com.manacommunity.parking.service;

import com.manacommunity.parking.dto.*;
import com.manacommunity.parking.engine.EvBillingEngine;
import com.manacommunity.parking.enums.ChargerStatus;
import com.manacommunity.parking.model.EvCharger;
import com.manacommunity.parking.model.EvChargingRate;
import com.manacommunity.parking.model.EvChargingSession;
import com.manacommunity.parking.model.EvTelemetryHeartbeat;
import com.manacommunity.parking.repository.EvChargerRepository;
import com.manacommunity.parking.repository.EvChargingRateRepository;
import com.manacommunity.parking.repository.EvChargingSessionRepository;
import com.manacommunity.parking.repository.EvTelemetryHeartbeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EvChargingService {

    private final EvChargerRepository chargerRepository;
    private final EvChargingRateRepository rateRepository;
    private final EvChargingSessionRepository sessionRepository;
    private final EvTelemetryHeartbeatRepository heartbeatRepository;
    private final EvBillingEngine billingEngine;

    @Transactional
    public EvChargingSessionResponse startSession(Long residentId, Long communityId, StartChargingRequest req) {
        EvCharger charger = chargerRepository.findById(req.chargerId())
                .orElseThrow(() -> new IllegalArgumentException("EvCharger not found: " + req.chargerId()));

        if (charger.getStatus() != ChargerStatus.AVAILABLE) {
            throw new IllegalStateException("Charger is currently " + charger.getStatus());
        }

        double startMeter = req.currentMeterReading() != null ? req.currentMeterReading() : (charger.getLatestMeterKwh() != null ? charger.getLatestMeterKwh() : 0.0);

        EvChargingSession session = EvChargingSession.builder()
                .communityId(communityId)
                .charger(charger)
                .residentId(residentId)
                .vehicleId(req.vehicleId())
                .startedAt(LocalDateTime.now())
                .startMeterKwh(startMeter)
                .status("ACTIVE")
                .build();

        session = sessionRepository.save(session);

        charger.setStatus(ChargerStatus.CHARGING);
        chargerRepository.save(charger);

        return EvChargingSessionResponse.from(session);
    }

    @Transactional
    public EvChargingSessionResponse stopSession(Long sessionId, StopChargingRequest req) {
        EvChargingSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        if (!"ACTIVE".equalsIgnoreCase(session.getStatus())) {
            throw new IllegalStateException("Session is not active");
        }

        LocalDateTime endedAt = LocalDateTime.now();
        double endMeter = req.finalMeterReading() != null ? req.finalMeterReading() : session.getStartMeterKwh();
        double totalKwh = billingEngine.calculateKwh(session.getStartMeterKwh(), endMeter);

        EvChargingRate rateConfig = rateRepository.findFirstByCommunityIdAndActiveTrue(session.getCommunityId())
                .orElse(null);

        BigDecimal totalCost = billingEngine.calculateCost(totalKwh, session.getStartedAt(), endedAt, rateConfig);

        session.setEndedAt(endedAt);
        session.setEndMeterKwh(endMeter);
        session.setTotalKwh(totalKwh);
        session.setTotalCost(totalCost);
        session.setStatus("COMPLETED");
        session.setStopReason(req.stopReason() != null ? req.stopReason() : "USER_STOP");

        session = sessionRepository.save(session);

        EvCharger charger = session.getCharger();
        charger.setStatus(ChargerStatus.AVAILABLE);
        charger.setCurrentPowerKw(BigDecimal.ZERO);
        charger.setLatestMeterKwh(endMeter);
        chargerRepository.save(charger);

        return EvChargingSessionResponse.from(session);
    }

    @Transactional
    public void processTelemetry(EvTelemetryRequest request) {
        EvCharger charger = chargerRepository.findByDeviceId(request.deviceId())
                .orElseThrow(() -> new IllegalArgumentException("No EV charger found for deviceId: " + request.deviceId()));

        Optional<EvChargingSession> activeSessionOpt = sessionRepository
                .findFirstByChargerIdAndStatus(charger.getId(), "ACTIVE");

        Long sessionId = activeSessionOpt.map(EvChargingSession::getId).orElse(null);

        EvTelemetryHeartbeat heartbeat = EvTelemetryHeartbeat.builder()
                .chargerId(charger.getId())
                .sessionId(sessionId)
                .timestamp(request.timestamp() != null ? request.timestamp() : LocalDateTime.now())
                .powerKw(request.powerKw() != null ? request.powerKw() : BigDecimal.ZERO)
                .voltage(request.voltage())
                .currentAmps(request.currentAmps())
                .temperatureCelsius(request.temperatureCelsius())
                .meterReadingKwh(request.meterReadingKwh())
                .stateOfChargePercent(request.stateOfChargePercent())
                .errorCode(request.errorCode())
                .build();

        heartbeatRepository.save(heartbeat);

        charger.setLatestMeterKwh(request.meterReadingKwh());
        if (request.powerKw() != null) charger.setCurrentPowerKw(request.powerKw());
        charger.setLastHeartbeatAt(request.timestamp() != null ? request.timestamp() : LocalDateTime.now());
        charger.setLastFaultCode(request.errorCode());

        if (request.errorCode() != null && !request.errorCode().isBlank()) {
            charger.setStatus(ChargerStatus.FAULT);
        } else if (activeSessionOpt.isPresent()) {
            BigDecimal p = request.powerKw() != null ? request.powerKw() : BigDecimal.ZERO;
            charger.setStatus(p.compareTo(new BigDecimal("0.05")) > 0 ? ChargerStatus.CHARGING : ChargerStatus.OCCUPIED_IDLE);
        } else {
            charger.setStatus(ChargerStatus.AVAILABLE);
        }

        chargerRepository.save(charger);
    }

    @Transactional(readOnly = true)
    public EvLiveTelemetryResponse getLiveTelemetry(Long chargerId) {
        EvCharger charger = chargerRepository.findById(chargerId)
                .orElseThrow(() -> new IllegalArgumentException("EvCharger not found: " + chargerId));

        List<EvTelemetryHeartbeat> recent = heartbeatRepository.findTop20ByChargerIdOrderByTimestampDesc(chargerId);
        EvTelemetryHeartbeat latest = recent.isEmpty() ? null : recent.get(0);

        return new EvLiveTelemetryResponse(
                charger.getId(),
                charger.getDeviceId(),
                charger.getStatus(),
                charger.getCurrentPowerKw(),
                latest != null ? latest.getVoltage() : null,
                latest != null ? latest.getCurrentAmps() : null,
                latest != null ? latest.getTemperatureCelsius() : null,
                charger.getLatestMeterKwh(),
                latest != null ? latest.getStateOfChargePercent() : null,
                charger.getLastHeartbeatAt()
        );
    }

    @Transactional(readOnly = true)
    public List<EvChargerResponse> getChargers(Long communityId) {
        return chargerRepository.findByCommunityIdOrderByCreatedAtDesc(communityId).stream()
                .map(EvChargerResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<EvChargingSessionResponse> getResidentSessions(Long residentId) {
        return sessionRepository.findByResidentIdOrderByStartedAtDesc(residentId).stream()
                .map(EvChargingSessionResponse::from).toList();
    }
}
