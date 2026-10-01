package com.manacommunity.parking.service;

import com.manacommunity.parking.dto.ParkingVisitorPassRequest;
import com.manacommunity.parking.enums.PassStatus;
import com.manacommunity.parking.enums.SlotStatus;
import com.manacommunity.parking.enums.SlotType;
import com.manacommunity.parking.enums.VehicleType;
import com.manacommunity.parking.model.ParkingSlot;
import com.manacommunity.parking.model.ParkingVisitorPass;
import com.manacommunity.parking.repository.ParkingSlotRepository;
import com.manacommunity.parking.repository.ParkingVisitorPassRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ParkingVisitorPassService {

    private final ParkingVisitorPassRepository passRepository;
    private final ParkingSlotRepository slotRepository;

    @Transactional
    public ParkingVisitorPass createVisitorPass(Long communityId, Long hostResidentId, ParkingVisitorPassRequest req) {
        // Auto-assign visitor slot if available
        List<ParkingSlot> visitorSlots = slotRepository.findByCommunityIdAndSlotTypeAndStatus(
                communityId, SlotType.VISITOR_STANDARD, SlotStatus.AVAILABLE);

        Long assignedSlotId = visitorSlots.isEmpty() ? null : visitorSlots.get(0).getId();

        ParkingVisitorPass pass = ParkingVisitorPass.builder()
                .passToken("PASS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .communityId(communityId)
                .hostResidentId(hostResidentId)
                .hostFlatNumber(req.hostFlatNumber())
                .visitorName(req.visitorName())
                .visitorPhone(req.visitorPhone())
                .visitorVehiclePlate(req.visitorVehiclePlate())
                .vehicleType(req.vehicleType() != null ? req.vehicleType() : VehicleType.VISITOR_CAR)
                .assignedSlotId(assignedSlotId)
                .validFrom(req.validFrom())
                .validUntil(req.validUntil())
                .status(PassStatus.ACTIVE)
                .build();

        return passRepository.save(pass);
    }

    @Transactional(readOnly = true)
    public List<ParkingVisitorPass> getResidentPasses(Long hostResidentId) {
        return passRepository.findByHostResidentIdOrderByValidFromDesc(hostResidentId);
    }
}
