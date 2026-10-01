package com.manacommunity.parking.service;

import com.manacommunity.parking.dto.AnprGateEventDto;
import com.manacommunity.parking.dto.AnprWebhookResponse;
import com.manacommunity.parking.engine.AnprPlateNormalizer;
import com.manacommunity.parking.enums.PassStatus;
import com.manacommunity.parking.model.AnprGateEvent;
import com.manacommunity.parking.model.ParkingSlot;
import com.manacommunity.parking.model.ParkingVisitorPass;
import com.manacommunity.parking.model.ResidentVehicle;
import com.manacommunity.parking.repository.AnprGateEventRepository;
import com.manacommunity.parking.repository.ParkingSlotRepository;
import com.manacommunity.parking.repository.ParkingVisitorPassRepository;
import com.manacommunity.parking.repository.ResidentVehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnprGateService {

    private final AnprGateEventRepository eventRepository;
    private final ResidentVehicleRepository vehicleRepository;
    private final ParkingVisitorPassRepository passRepository;
    private final ParkingSlotRepository slotRepository;
    private final AnprPlateNormalizer normalizer;

    @Transactional
    public AnprWebhookResponse processGateCapture(Long communityId, AnprGateEventDto req) {
        String cleanPlate = normalizer.normalize(req.rawLicensePlate());
        LocalDateTime now = req.timestamp() != null ? req.timestamp() : LocalDateTime.now();

        // 1. Check if resident vehicle
        Optional<ResidentVehicle> residentVehicleOpt = vehicleRepository.findByLicensePlate(cleanPlate);
        if (residentVehicleOpt.isPresent()) {
            ResidentVehicle rv = residentVehicleOpt.get();
            String slotNum = rv.getAllocatedSlotId() != null 
                    ? slotRepository.findById(rv.getAllocatedSlotId()).map(ParkingSlot::getSlotNumber).orElse("N/A")
                    : "N/A";

            saveEvent(communityId, req, cleanPlate, "RESIDENT", rv.getId(), true);
            return new AnprWebhookResponse(true, "RESIDENT", "Authorized Resident Access", cleanPlate, slotNum);
        }

        // 2. Check if valid visitor pass
        Optional<ParkingVisitorPass> passOpt = passRepository
                .findFirstByVisitorVehiclePlateAndStatusAndValidFromBeforeAndValidUntilAfter(
                        cleanPlate, PassStatus.ACTIVE, now, now);

        if (passOpt.isPresent()) {
            ParkingVisitorPass pass = passOpt.get();
            if ("ENTRY".equalsIgnoreCase(req.eventType()) && pass.getEntryTime() == null) {
                pass.setEntryTime(now);
                passRepository.save(pass);
            } else if ("EXIT".equalsIgnoreCase(req.eventType())) {
                pass.setExitTime(now);
                pass.setStatus(PassStatus.USED);
                passRepository.save(pass);
            }

            String slotNum = pass.getAssignedSlotId() != null
                    ? slotRepository.findById(pass.getAssignedSlotId()).map(ParkingSlot::getSlotNumber).orElse("Visitor Bay")
                    : "Visitor Bay";

            saveEvent(communityId, req, cleanPlate, "VISITOR", pass.getId(), true);
            return new AnprWebhookResponse(true, "VISITOR", "Valid Visitor Pass", cleanPlate, slotNum);
        }

        // 3. Unauthorized vehicle
        saveEvent(communityId, req, cleanPlate, "UNAUTHORIZED", null, false);
        return new AnprWebhookResponse(false, "UNAUTHORIZED", "Vehicle Not Registered", cleanPlate, null);
    }

    private void saveEvent(Long communityId, AnprGateEventDto req, String cleanPlate, String matchType, Long refId, boolean open) {
        AnprGateEvent event = AnprGateEvent.builder()
                .communityId(communityId)
                .gateId(req.gateId())
                .rawLicensePlate(req.rawLicensePlate())
                .normalizedLicensePlate(cleanPlate)
                .eventType(req.eventType())
                .confidence(req.confidence())
                .barrierOpened(open)
                .matchType(matchType)
                .matchedRefId(refId)
                .snapshotUrl(req.snapshotUrl())
                .timestamp(req.timestamp() != null ? req.timestamp() : LocalDateTime.now())
                .build();
        eventRepository.save(event);
    }
}
