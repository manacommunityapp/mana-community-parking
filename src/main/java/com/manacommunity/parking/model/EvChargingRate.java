package com.manacommunity.parking.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "parking_ev_charging_rates")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvChargingRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long communityId;

    @Column(nullable = false, precision = 6, scale = 2)
    @Builder.Default
    private BigDecimal unitRatePerKwh = new BigDecimal("8.50");

    @Column(nullable = false, precision = 6, scale = 2)
    @Builder.Default
    private BigDecimal peakRatePerKwh = new BigDecimal("12.00");

    @Builder.Default
    private Integer peakStartHour = 18;

    @Builder.Default
    private Integer peakEndHour = 22;

    @Column(nullable = false, precision = 6, scale = 2)
    @Builder.Default
    private BigDecimal minimumBillAmount = new BigDecimal("20.00");

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
}
