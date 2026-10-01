package com.manacommunity.parking.repository;

import com.manacommunity.parking.model.EvTelemetryHeartbeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvTelemetryHeartbeatRepository extends JpaRepository<EvTelemetryHeartbeat, Long> {
    List<EvTelemetryHeartbeat> findTop20ByChargerIdOrderByTimestampDesc(Long chargerId);
    List<EvTelemetryHeartbeat> findBySessionIdOrderByTimestampAsc(Long sessionId);
}
