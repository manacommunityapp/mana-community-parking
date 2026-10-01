package com.manacommunity.parking.controller;

import com.manacommunity.parking.config.UserPrincipal;
import com.manacommunity.parking.dto.AllocateSlotRequest;
import com.manacommunity.parking.dto.ParkingSlotDto;
import com.manacommunity.parking.service.ParkingSlotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parking/slots")
@RequiredArgsConstructor
public class ParkingSlotController {

    private final ParkingSlotService slotService;

    @GetMapping
    public ResponseEntity<List<ParkingSlotDto>> getSlots(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(slotService.getSlots(principal.getCommunityId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ParkingSlotDto> getSlot(@PathVariable Long id) {
        return ResponseEntity.ok(slotService.getSlotById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ParkingSlotDto> createSlot(
            @Valid @RequestBody ParkingSlotDto req,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(slotService.createSlot(principal.getCommunityId(), req));
    }

    @PostMapping("/allocate")
    public ResponseEntity<ParkingSlotDto> allocateBestSlot(
            @Valid @RequestBody AllocateSlotRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(slotService.allocateBestSlot(principal.getCommunityId(), req));
    }

    @PostMapping("/{id}/release")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ParkingSlotDto> releaseSlot(@PathVariable Long id) {
        return ResponseEntity.ok(slotService.releaseSlot(id));
    }
}
