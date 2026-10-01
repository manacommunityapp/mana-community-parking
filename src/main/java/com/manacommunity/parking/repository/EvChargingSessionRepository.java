package com.manacommunity.parking.repository;

import com.manacommunity.parking.model.EvChargingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EvChargingSessionRepository extends JpaRepository<EvChargingSession, Long> {
    List<EvChargingSession> findByResidentIdOrderByStartedAtDesc(Long residentId);
    Optional<EvChargingSession> findFirstByResidentIdAndStatus(Long residentId, String status);
    Optional<EvChargingSession> findFirstByChargerIdAndStatus(Long chargerId, String status);
    List<EvChargingSession> findByCommunityIdOrderByStartedAtDesc(Long communityId);
}
