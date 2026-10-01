package com.manacommunity.parking.repository;

import com.manacommunity.parking.enums.SlotStatus;
import com.manacommunity.parking.enums.SlotType;
import com.manacommunity.parking.model.ParkingSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParkingSlotRepository extends JpaRepository<ParkingSlot, Long> {
    List<ParkingSlot> findByCommunityIdAndActiveTrue(Long communityId);
    List<ParkingSlot> findByCommunityIdAndStatus(Long communityId, SlotStatus status);
    List<ParkingSlot> findByCommunityIdAndSlotTypeAndStatus(Long communityId, SlotType slotType, SlotStatus status);
    Optional<ParkingSlot> findByCommunityIdAndSlotNumber(Long communityId, String slotNumber);
    List<ParkingSlot> findByAllocatedResidentId(Long residentId);
}
