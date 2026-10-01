package com.manacommunity.parking.dto;

import com.manacommunity.parking.enums.SlotStatus;
import com.manacommunity.parking.enums.SlotType;
import com.manacommunity.parking.enums.VehicleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record ParkingSlotDto(
    Long id,
    Long communityId,
    @NotBlank String slotNumber,
    String tower,
    String basementLevel,
    SlotType slotType,
    VehicleType allowedVehicleType,
    SlotStatus status,
    Long allocatedResidentId,
    String allocatedFlatNumber,
    String currentVehiclePlate,
    Boolean hasEvCharger,
    LocalDateTime allocatedAt
) {}
