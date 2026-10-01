package com.manacommunity.parking.model;

import com.manacommunity.parking.enums.PassStatus;
import com.manacommunity.parking.enums.VehicleType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "parking_visitor_passes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParkingVisitorPass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 36)
    private String passToken;

    @Column(nullable = false)
    private Long communityId;

    @Column(nullable = false)
    private Long hostResidentId;

    @Column(length = 32)
    private String hostFlatNumber;

    @Column(nullable = false, length = 64)
    private String visitorName;

    @Column(length = 20)
    private String visitorPhone;

    @Column(nullable = false, length = 32)
    private String visitorVehiclePlate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    @Builder.Default
    private VehicleType vehicleType = VehicleType.VISITOR_CAR;

    private Long assignedSlotId;

    @Column(nullable = false)
    private LocalDateTime validFrom;

    @Column(nullable = false)
    private LocalDateTime validUntil;

    private LocalDateTime entryTime;

    private LocalDateTime exitTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    @Builder.Default
    private PassStatus status = PassStatus.ACTIVE;
}
