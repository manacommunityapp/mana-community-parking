package com.manacommunity.parking.controller;

import com.manacommunity.parking.dto.AnprGateEventDto;
import com.manacommunity.parking.dto.AnprWebhookResponse;
import com.manacommunity.parking.service.AnprGateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/parking/anpr")
@RequiredArgsConstructor
public class AnprGateController {

    private final AnprGateService anprGateService;

    @PostMapping("/webhook")
    public ResponseEntity<AnprWebhookResponse> handleGateCapture(
            @RequestParam(defaultValue = "1") Long communityId,
            @Valid @RequestBody AnprGateEventDto req) {
        return ResponseEntity.ok(anprGateService.processGateCapture(communityId, req));
    }
}
