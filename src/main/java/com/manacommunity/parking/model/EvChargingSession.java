package com.manacommunity.parking.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "parking_ev_charging_sessions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvChargingSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long communityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "charger_id", nullable = false)
    private EvCharger charger;

    @Column(nullable = false)
    private Long residentId;

    private Long vehicleId;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    private LocalDateTime endedAt;

    @Column(nullable = false)
    private Double startMeterKwh;

    private Double endMeterKwh;

    private Double totalKwh;

    @Column(precision = 10, scale = 2)
    private BigDecimal totalCost;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "ACTIVE"; // ACTIVE, COMPLETED, CANCELLED, FAILED

    private Long cfbosWalletTransactionId;

    @Column(length = 100)
    private String stopReason;
}
