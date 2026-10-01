package com.manacommunity.parking.dto;

import com.manacommunity.parking.enums.VehicleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record ParkingVisitorPassRequest(
    @NotBlank String visitorName,
    String visitorPhone,
    @NotBlank String visitorVehiclePlate,
    VehicleType vehicleType,
    @NotNull LocalDateTime validFrom,
    @NotNull LocalDateTime validUntil,
    String hostFlatNumber
) {}
