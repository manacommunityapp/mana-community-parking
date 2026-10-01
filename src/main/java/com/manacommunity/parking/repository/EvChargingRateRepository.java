package com.manacommunity.parking.repository;

import com.manacommunity.parking.model.EvChargingRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EvChargingRateRepository extends JpaRepository<EvChargingRate, Long> {
    Optional<EvChargingRate> findFirstByCommunityIdAndActiveTrue(Long communityId);
}
