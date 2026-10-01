package com.manacommunity.parking.repository;

import com.manacommunity.parking.model.EvCharger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EvChargerRepository extends JpaRepository<EvCharger, Long> {
    Optional<EvCharger> findByDeviceId(String deviceId);
    List<EvCharger> findByCommunityIdOrderByCreatedAtDesc(Long communityId);
    Optional<EvCharger> findByParkingSlotId(Long slotId);
}
