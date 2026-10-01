package com.manacommunity.parking.controller;

import com.manacommunity.parking.config.UserPrincipal;
import com.manacommunity.parking.dto.ParkingViolationDto;
import com.manacommunity.parking.enums.ViolationStatus;
import com.manacommunity.parking.service.ParkingViolationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parking/violations")
@RequiredArgsConstructor
public class ParkingViolationController {

    private final ParkingViolationService violationService;

    @GetMapping
    public ResponseEntity<List<ParkingViolationDto>> getViolations(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(violationService.getViolations(principal.getCommunityId()));
    }

    @PostMapping
    public ResponseEntity<ParkingViolationDto> reportViolation(
            @Valid @RequestBody ParkingViolationDto req,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(violationService.reportViolation(principal.getCommunityId(), principal.getId(), req));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ParkingViolationDto> resolveViolation(
            @PathVariable Long id,
            @RequestParam ViolationStatus status) {
        return ResponseEntity.ok(violationService.resolveViolation(id, status));
    }
}
