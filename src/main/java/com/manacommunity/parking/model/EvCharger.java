package com.manacommunity.parking.model;

import com.manacommunity.parking.enums.ChargerStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "parking_ev_chargers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvCharger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long communityId;

    private Long parkingSlotId;

    @Column(nullable = false, unique = true, length = 60)
    private String deviceId;

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String connectorType = "TYPE2"; // TYPE2, CCS2, CHADEMO, GB/T

    @Column(nullable = false)
    @Builder.Default
    private Double maxKwRating = 7.4;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ChargerStatus status = ChargerStatus.AVAILABLE;

    @Column(precision = 8, scale = 2)
    @Builder.Default
    private BigDecimal currentPowerKw = BigDecimal.ZERO;

    private Double latestMeterKwh;

    @Column(length = 64)
    private String lastFaultCode;

    private LocalDateTime lastHeartbeatAt;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
