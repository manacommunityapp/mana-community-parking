package com.manacommunity.parking.repository;

import com.manacommunity.parking.enums.PassStatus;
import com.manacommunity.parking.model.ParkingVisitorPass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ParkingVisitorPassRepository extends JpaRepository<ParkingVisitorPass, Long> {
    Optional<ParkingVisitorPass> findByPassToken(String passToken);
    Optional<ParkingVisitorPass> findFirstByVisitorVehiclePlateAndStatusAndValidFromBeforeAndValidUntilAfter(
            String plate, PassStatus status, LocalDateTime start, LocalDateTime end);
    List<ParkingVisitorPass> findByHostResidentIdOrderByValidFromDesc(Long hostResidentId);
    List<ParkingVisitorPass> findByCommunityIdOrderByValidFromDesc(Long communityId);
}
