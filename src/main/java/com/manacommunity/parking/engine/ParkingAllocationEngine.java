package com.manacommunity.parking.engine;

import com.manacommunity.parking.enums.SlotStatus;
import com.manacommunity.parking.enums.SlotType;
import com.manacommunity.parking.enums.VehicleType;
import com.manacommunity.parking.model.ParkingSlot;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Component
public class ParkingAllocationEngine {

    /**
     * Matches the best available parking slot for a vehicle based on EV requirements,
     * vehicle dimensions, and building tower proximity.
     */
    public Optional<ParkingSlot> findBestSlot(List<ParkingSlot> availableSlots, VehicleType vehicleType, String preferredTower) {
        if (availableSlots == null || availableSlots.isEmpty()) {
            return Optional.empty();
        }

        boolean requiresEv = (vehicleType == VehicleType.EV_FOUR_WHEELER || vehicleType == VehicleType.EV_TWO_WHEELER);

        return availableSlots.stream()
                .filter(s -> s.getStatus() == SlotStatus.AVAILABLE)
                .filter(s -> !requiresEv || Boolean.TRUE.equals(s.getHasEvCharger()) || s.getSlotType() == SlotType.RESIDENT_EV)
                .filter(s -> isVehicleCompatible(s, vehicleType))
                .min(Comparator
                        .comparing((ParkingSlot s) -> preferredTower != null && preferredTower.equalsIgnoreCase(s.getTower()) ? 0 : 1)
                        .thenComparing(s -> requiresEv && Boolean.TRUE.equals(s.getHasEvCharger()) ? 0 : 1)
                        .thenComparing(ParkingSlot::getSlotNumber)
                );
    }

    private boolean isVehicleCompatible(ParkingSlot slot, VehicleType vehicleType) {
        if (vehicleType == VehicleType.TWO_WHEELER || vehicleType == VehicleType.EV_TWO_WHEELER) {
            return slot.getAllowedVehicleType() == VehicleType.TWO_WHEELER || slot.getAllowedVehicleType() == VehicleType.EV_TWO_WHEELER;
        }
        if (vehicleType == VehicleType.FOUR_WHEELER_SUV) {
            return slot.getAllowedVehicleType() == VehicleType.FOUR_WHEELER_SUV || slot.getAllowedVehicleType() == VehicleType.EV_FOUR_WHEELER;
        }
        return true;
    }
}
