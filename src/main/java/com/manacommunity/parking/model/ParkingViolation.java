package com.manacommunity.parking.model;

import com.manacommunity.parking.enums.ViolationStatus;
import com.manacommunity.parking.enums.ViolationType;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "parking_violations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParkingViolation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long communityId;

    @Column(nullable = false, length = 32)
    private String licensePlate;

    private Long slotId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ViolationType violationType;

    @Column(length = 500)
    private String description;

    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal fineAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    @Builder.Default
    private ViolationStatus status = ViolationStatus.REPORTED;

    private Long reportedByUserId;

    @Column(length = 500)
    private String photoUrl;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime reportedAt = LocalDateTime.now();

    private LocalDateTime resolvedAt;
}
