package com.manacommunity.parking.dto;

import com.manacommunity.parking.enums.VehicleType;
import jakarta.validation.constraints.NotNull;

public record AllocateSlotRequest(
    @NotNull Long residentId,
    String flatNumber,
    String licensePlate,
    @NotNull VehicleType vehicleType,
    String preferredTower
) {}
