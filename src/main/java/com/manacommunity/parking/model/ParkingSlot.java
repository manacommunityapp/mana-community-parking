package com.manacommunity.parking.model;

import com.manacommunity.parking.enums.SlotStatus;
import com.manacommunity.parking.enums.SlotType;
import com.manacommunity.parking.enums.VehicleType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "parking_slots")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParkingSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long communityId;

    @Column(nullable = false, length = 32)
    private String slotNumber; // e.g. "B1-A101"

    @Column(length = 32)
    private String tower; // e.g. "Tower A"

    @Column(length = 32)
    private String basementLevel; // "B1", "B2", "Ground"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    @Builder.Default
    private SlotType slotType = SlotType.RESIDENT_STANDARD;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    @Builder.Default
    private VehicleType allowedVehicleType = VehicleType.FOUR_WHEELER_SEDAN;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    @Builder.Default
    private SlotStatus status = SlotStatus.AVAILABLE;

    private Long allocatedResidentId;

    @Column(length = 32)
    private String allocatedFlatNumber;

    @Column(length = 32)
    private String currentVehiclePlate;

    @Builder.Default
    private Boolean hasEvCharger = false;

    private LocalDateTime allocatedAt;

    private LocalDateTime lastOccupiedAt;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
}
