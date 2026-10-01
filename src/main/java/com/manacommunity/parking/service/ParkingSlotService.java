package com.manacommunity.parking.service;

import com.manacommunity.parking.dto.AllocateSlotRequest;
import com.manacommunity.parking.dto.ParkingSlotDto;
import com.manacommunity.parking.engine.ParkingAllocationEngine;
import com.manacommunity.parking.enums.SlotStatus;
import com.manacommunity.parking.enums.SlotType;
import com.manacommunity.parking.model.ParkingSlot;
import com.manacommunity.parking.model.ResidentVehicle;
import com.manacommunity.parking.repository.ParkingSlotRepository;
import com.manacommunity.parking.repository.ResidentVehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ParkingSlotService {

    private final ParkingSlotRepository slotRepository;
    private final ResidentVehicleRepository vehicleRepository;
    private final ParkingAllocationEngine allocationEngine;

    @Transactional(readOnly = true)
    public List<ParkingSlotDto> getSlots(Long communityId) {
        return slotRepository.findByCommunityIdAndActiveTrue(communityId).stream()
                .map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public ParkingSlotDto getSlotById(Long slotId) {
        ParkingSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new IllegalArgumentException("Parking Slot not found: " + slotId));
        return toDto(slot);
    }

    @Transactional
    public ParkingSlotDto createSlot(Long communityId, ParkingSlotDto req) {
        ParkingSlot slot = ParkingSlot.builder()
                .communityId(communityId)
                .slotNumber(req.slotNumber())
                .tower(req.tower())
                .basementLevel(req.basementLevel())
                .slotType(req.slotType() != null ? req.slotType() : SlotType.RESIDENT_STANDARD)
                .allowedVehicleType(req.allowedVehicleType())
                .status(SlotStatus.AVAILABLE)
                .hasEvCharger(req.hasEvCharger() != null ? req.hasEvCharger() : false)
                .active(true)
                .build();
        return toDto(slotRepository.save(slot));
    }

    @Transactional
    public ParkingSlotDto allocateBestSlot(Long communityId, AllocateSlotRequest req) {
        List<ParkingSlot> availableSlots = slotRepository.findByCommunityIdAndStatus(communityId, SlotStatus.AVAILABLE);
        ParkingSlot bestSlot = allocationEngine.findBestSlot(availableSlots, req.vehicleType(), req.preferredTower())
                .orElseThrow(() -> new IllegalStateException("No compatible parking slot available for vehicle type " + req.vehicleType()));

        bestSlot.setStatus(SlotStatus.OCCUPIED);
        bestSlot.setAllocatedResidentId(req.residentId());
        bestSlot.setAllocatedFlatNumber(req.flatNumber());
        bestSlot.setCurrentVehiclePlate(req.licensePlate());
        bestSlot.setAllocatedAt(LocalDateTime.now());

        if (req.licensePlate() != null && !req.licensePlate().isBlank()) {
            vehicleRepository.findByLicensePlate(req.licensePlate()).ifPresent(v -> {
                v.setAllocatedSlotId(bestSlot.getId());
                vehicleRepository.save(v);
            });
        }

        return toDto(slotRepository.save(bestSlot));
    }

    @Transactional
    public ParkingSlotDto releaseSlot(Long slotId) {
        ParkingSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new IllegalArgumentException("Slot not found: " + slotId));

        slot.setStatus(SlotStatus.AVAILABLE);
        slot.setAllocatedResidentId(null);
        slot.setAllocatedFlatNumber(null);
        slot.setCurrentVehiclePlate(null);
        slot.setAllocatedAt(null);

        return toDto(slotRepository.save(slot));
    }

    private ParkingSlotDto toDto(ParkingSlot s) {
        return new ParkingSlotDto(
                s.getId(),
                s.getCommunityId(),
                s.getSlotNumber(),
                s.getTower(),
                s.getBasementLevel(),
                s.getSlotType(),
                s.getAllowedVehicleType(),
                s.getStatus(),
                s.getAllocatedResidentId(),
                s.getAllocatedFlatNumber(),
                s.getCurrentVehiclePlate(),
                s.getHasEvCharger(),
                s.getAllocatedAt()
        );
    }
}
