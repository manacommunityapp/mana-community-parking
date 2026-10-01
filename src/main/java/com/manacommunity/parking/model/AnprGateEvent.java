package com.manacommunity.parking.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "parking_anpr_gate_events")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnprGateEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long communityId;

    @Column(nullable = false, length = 64)
    private String gateId; // "GATE_MAIN_IN", "GATE_EAST_OUT"

    @Column(nullable = false, length = 32)
    private String rawLicensePlate;

    @Column(nullable = false, length = 32)
    private String normalizedLicensePlate;

    @Column(nullable = false, length = 16)
    private String eventType; // "ENTRY", "EXIT"

    @Column(nullable = false)
    private Double confidence; // 0.0 - 1.0

    @Column(nullable = false)
    @Builder.Default
    private Boolean barrierOpened = false;

    @Column(length = 64)
    private String matchType; // "RESIDENT", "VISITOR", "VENDOR", "UNAUTHORIZED"

    private Long matchedRefId;

    @Column(length = 500)
    private String snapshotUrl;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
