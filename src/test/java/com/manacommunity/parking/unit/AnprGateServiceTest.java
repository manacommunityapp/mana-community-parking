package com.manacommunity.parking.unit;

import com.manacommunity.parking.dto.AnprGateEventDto;
import com.manacommunity.parking.dto.AnprWebhookResponse;
import com.manacommunity.parking.engine.AnprPlateNormalizer;
import com.manacommunity.parking.enums.PassStatus;
import com.manacommunity.parking.enums.VehicleType;
import com.manacommunity.parking.model.ParkingSlot;
import com.manacommunity.parking.model.ParkingVisitorPass;
import com.manacommunity.parking.model.ResidentVehicle;
import com.manacommunity.parking.repository.AnprGateEventRepository;
import com.manacommunity.parking.repository.ParkingSlotRepository;
import com.manacommunity.parking.repository.ParkingVisitorPassRepository;
import com.manacommunity.parking.repository.ResidentVehicleRepository;
import com.manacommunity.parking.service.AnprGateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AnprGateService Unit Tests")
class AnprGateServiceTest {

    @Mock private AnprGateEventRepository eventRepository;
    @Mock private ResidentVehicleRepository vehicleRepository;
    @Mock private ParkingVisitorPassRepository passRepository;
    @Mock private ParkingSlotRepository slotRepository;
    @Spy private AnprPlateNormalizer normalizer = new AnprPlateNormalizer();

    @InjectMocks
    private AnprGateService service;

    @Test
    @DisplayName("processGateCapture: opens barrier for recognized resident vehicle")
    void processGateCapture_residentVehicle() {
        ResidentVehicle rv = ResidentVehicle.builder().id(10L).licensePlate("KA01MJ5000").allocatedSlotId(101L).build();
        when(vehicleRepository.findByLicensePlate("KA01MJ5000")).thenReturn(Optional.of(rv));
        when(slotRepository.findById(101L)).thenReturn(Optional.of(ParkingSlot.builder().slotNumber("A-101").build()));

        AnprGateEventDto req = new AnprGateEventDto(null, 1L, "GATE-1", "KA 01 MJ 5000", null, "ENTRY", 0.95, false, null, null, null, LocalDateTime.now());
        AnprWebhookResponse response = service.processGateCapture(1L, req);

        assertThat(response.openBarrier()).isTrue();
        assertThat(response.matchType()).isEqualTo("RESIDENT");
        assertThat(response.assignedSlotNumber()).isEqualTo("A-101");
        verify(eventRepository).save(any());
    }

    @Test
    @DisplayName("processGateCapture: blocks barrier for unauthorized vehicle")
    void processGateCapture_unauthorized() {
        when(vehicleRepository.findByLicensePlate("DL01XX9999")).thenReturn(Optional.empty());
        when(passRepository.findFirstByVisitorVehiclePlateAndStatusAndValidFromBeforeAndValidUntilAfter(any(), any(), any(), any()))
                .thenReturn(Optional.empty());

        AnprGateEventDto req = new AnprGateEventDto(null, 1L, "GATE-1", "DL01XX9999", null, "ENTRY", 0.90, false, null, null, null, LocalDateTime.now());
        AnprWebhookResponse response = service.processGateCapture(1L, req);

        assertThat(response.openBarrier()).isFalse();
        assertThat(response.matchType()).isEqualTo("UNAUTHORIZED");
        verify(eventRepository).save(any());
    }
}
