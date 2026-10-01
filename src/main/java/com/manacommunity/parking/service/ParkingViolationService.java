package com.manacommunity.parking.service;

import com.manacommunity.parking.dto.ParkingViolationDto;
import com.manacommunity.parking.engine.ParkingViolationEngine;
import com.manacommunity.parking.enums.ViolationStatus;
import com.manacommunity.parking.model.ParkingViolation;
import com.manacommunity.parking.repository.ParkingViolationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ParkingViolationService {

    private final ParkingViolationRepository violationRepository;
    private final ParkingViolationEngine violationEngine;

    @Transactional(readOnly = true)
    public List<ParkingViolationDto> getViolations(Long communityId) {
        return violationRepository.findByCommunityIdOrderByReportedAtDesc(communityId).stream()
                .map(this::toDto).toList();
    }

    @Transactional
    public ParkingViolationDto reportViolation(Long communityId, Long reporterUserId, ParkingViolationDto req) {
        List<ParkingViolation> history = violationRepository.findByLicensePlateOrderByReportedAtDesc(req.licensePlate());
        BigDecimal fine = violationEngine.calculateFine(req.violationType(), history.size());

        ParkingViolation v = ParkingViolation.builder()
                .communityId(communityId)
                .licensePlate(req.licensePlate())
                .slotId(req.slotId())
                .violationType(req.violationType())
                .description(req.description())
                .fineAmount(fine)
                .status(ViolationStatus.REPORTED)
                .reportedByUserId(reporterUserId)
                .photoUrl(req.photoUrl())
                .reportedAt(LocalDateTime.now())
                .build();

        return toDto(violationRepository.save(v));
    }

    @Transactional
    public ParkingViolationDto resolveViolation(Long violationId, ViolationStatus status) {
        ParkingViolation v = violationRepository.findById(violationId)
                .orElseThrow(() -> new IllegalArgumentException("Violation not found: " + violationId));

        v.setStatus(status);
        v.setResolvedAt(LocalDateTime.now());
        return toDto(violationRepository.save(v));
    }

    private ParkingViolationDto toDto(ParkingViolation v) {
        return new ParkingViolationDto(
                v.getId(),
                v.getCommunityId(),
                v.getLicensePlate(),
                v.getSlotId(),
                v.getViolationType(),
                v.getDescription(),
                v.getFineAmount(),
                v.getStatus(),
                v.getReportedByUserId(),
                v.getPhotoUrl(),
                v.getReportedAt(),
                v.getResolvedAt()
        );
    }
}
