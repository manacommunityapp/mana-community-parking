package com.manacommunity.parking.model;

import com.manacommunity.parking.enums.VehicleType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "parking_resident_vehicles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResidentVehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long communityId;

    @Column(nullable = false)
    private Long residentId;

    @Column(nullable = false, unique = true, length = 32)
    private String licensePlate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private VehicleType vehicleType;

    @Column(length = 64)
    private String makeAndModel; // e.g. "Tata Nexon EV"

    @Column(length = 32)
    private String color;

    private Long allocatedSlotId;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime registeredAt = LocalDateTime.now();
}
