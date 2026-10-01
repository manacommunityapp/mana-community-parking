package com.manacommunity.parking.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "parking_ev_telemetry_heartbeats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvTelemetryHeartbeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long chargerId;

    private Long sessionId;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(precision = 8, scale = 2)
    private BigDecimal powerKw;

    @Column(precision = 6, scale = 2)
    private BigDecimal voltage;

    @Column(precision = 6, scale = 2)
    private BigDecimal currentAmps;

    @Column(precision = 5, scale = 1)
    private BigDecimal temperatureCelsius;

    private Double meterReadingKwh;

    @Column(precision = 5, scale = 2)
    private BigDecimal stateOfChargePercent;

    @Column(length = 64)
    private String errorCode;
}
