package com.manacommunity.parking.unit;

import com.manacommunity.parking.engine.ParkingAllocationEngine;
import com.manacommunity.parking.enums.SlotStatus;
import com.manacommunity.parking.enums.SlotType;
import com.manacommunity.parking.enums.VehicleType;
import com.manacommunity.parking.model.ParkingSlot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ParkingAllocationEngine Unit Tests")
class ParkingAllocationEngineTest {

    private ParkingAllocationEngine engine;

    @BeforeEach
    void setUp() {
        engine = new ParkingAllocationEngine();
    }

    @Test
    @DisplayName("findBestSlot: prioritizes preferred tower")
    void findBestSlot_prefersTower() {
        ParkingSlot s1 = ParkingSlot.builder().id(1L).slotNumber("B1-A1").tower("Tower B").status(SlotStatus.AVAILABLE).allowedVehicleType(VehicleType.FOUR_WHEELER_SEDAN).build();
        ParkingSlot s2 = ParkingSlot.builder().id(2L).slotNumber("A1-A1").tower("Tower A").status(SlotStatus.AVAILABLE).allowedVehicleType(VehicleType.FOUR_WHEELER_SEDAN).build();

        Optional<ParkingSlot> best = engine.findBestSlot(List.of(s1, s2), VehicleType.FOUR_WHEELER_SEDAN, "Tower A");
        assertThat(best).isPresent();
        assertThat(best.get().getId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("findBestSlot: allocates slot with EV charger for EV vehicles")
    void findBestSlot_allocatesEvChargerSlot() {
        ParkingSlot s1 = ParkingSlot.builder().id(1L).slotNumber("A1-01").tower("Tower A").status(SlotStatus.AVAILABLE).hasEvCharger(false).build();
        ParkingSlot s2 = ParkingSlot.builder().id(2L).slotNumber("A1-02").tower("Tower A").status(SlotStatus.AVAILABLE).hasEvCharger(true).slotType(SlotType.RESIDENT_EV).build();

        Optional<ParkingSlot> best = engine.findBestSlot(List.of(s1, s2), VehicleType.EV_FOUR_WHEELER, "Tower A");
        assertThat(best).isPresent();
        assertThat(best.get().getId()).isEqualTo(2L);
    }
}
