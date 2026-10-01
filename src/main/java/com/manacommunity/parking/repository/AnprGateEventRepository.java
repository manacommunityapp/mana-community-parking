package com.manacommunity.parking.repository;

import com.manacommunity.parking.model.AnprGateEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnprGateEventRepository extends JpaRepository<AnprGateEvent, Long> {
    List<AnprGateEvent> findTop50ByCommunityIdOrderByTimestampDesc(Long communityId);
    List<AnprGateEvent> findByNormalizedLicensePlateOrderByTimestampDesc(String plate);
}
