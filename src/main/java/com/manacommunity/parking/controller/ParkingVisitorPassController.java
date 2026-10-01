package com.manacommunity.parking.controller;

import com.manacommunity.parking.config.UserPrincipal;
import com.manacommunity.parking.dto.ParkingVisitorPassRequest;
import com.manacommunity.parking.model.ParkingVisitorPass;
import com.manacommunity.parking.service.ParkingVisitorPassService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parking/visitor-passes")
@RequiredArgsConstructor
public class ParkingVisitorPassController {

    private final ParkingVisitorPassService passService;

    @PostMapping
    public ResponseEntity<ParkingVisitorPass> createPass(
            @Valid @RequestBody ParkingVisitorPassRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(passService.createVisitorPass(principal.getCommunityId(), principal.getId(), req));
    }

    @GetMapping("/my")
    public ResponseEntity<List<ParkingVisitorPass>> getMyPasses(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(passService.getResidentPasses(principal.getId()));
    }
}
