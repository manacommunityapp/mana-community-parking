package com.manacommunity.parking.controller;

import com.manacommunity.parking.config.UserPrincipal;
import com.manacommunity.parking.dto.*;
import com.manacommunity.parking.service.EvChargingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parking/ev")
@RequiredArgsConstructor
public class EvChargingController {

    private final EvChargingService evChargingService;

    @PostMapping("/telemetry")
    public ResponseEntity<Void> recordTelemetry(@Valid @RequestBody EvTelemetryRequest request) {
        evChargingService.processTelemetry(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/sessions/start")
    public ResponseEntity<EvChargingSessionResponse> startSession(
            @Valid @RequestBody StartChargingRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(evChargingService.startSession(principal.getId(), principal.getCommunityId(), request));
    }

    @PostMapping("/sessions/{sessionId}/stop")
    public ResponseEntity<EvChargingSessionResponse> stopSession(
            @PathVariable Long sessionId,
            @Valid @RequestBody StopChargingRequest request) {
        return ResponseEntity.ok(evChargingService.stopSession(sessionId, request));
    }

    @GetMapping("/chargers")
    public ResponseEntity<List<EvChargerResponse>> getChargers(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(evChargingService.getChargers(principal.getCommunityId()));
    }

    @GetMapping("/chargers/{chargerId}/live")
    public ResponseEntity<EvLiveTelemetryResponse> getLiveTelemetry(@PathVariable Long chargerId) {
        return ResponseEntity.ok(evChargingService.getLiveTelemetry(chargerId));
    }

    @GetMapping("/sessions/my")
    public ResponseEntity<List<EvChargingSessionResponse>> getMySessions(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(evChargingService.getResidentSessions(principal.getId()));
    }
}
