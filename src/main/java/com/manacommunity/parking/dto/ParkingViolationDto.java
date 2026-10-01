package com.manacommunity.parking.dto;

import com.manacommunity.parking.enums.ViolationStatus;
import com.manacommunity.parking.enums.ViolationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ParkingViolationDto(
    Long id,
    Long communityId,
    @NotBlank String licensePlate,
    Long slotId,
    @NotNull ViolationType violationType,
    String description,
    BigDecimal fineAmount,
    ViolationStatus status,
    Long reportedByUserId,
    String photoUrl,
    LocalDateTime reportedAt,
    LocalDateTime resolvedAt
) {}
