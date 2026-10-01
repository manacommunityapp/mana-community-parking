package com.manacommunity.parking.repository;

import com.manacommunity.parking.enums.ViolationStatus;
import com.manacommunity.parking.model.ParkingViolation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParkingViolationRepository extends JpaRepository<ParkingViolation, Long> {
    List<ParkingViolation> findByCommunityIdOrderByReportedAtDesc(Long communityId);
    List<ParkingViolation> findByLicensePlateOrderByReportedAtDesc(String licensePlate);
    List<ParkingViolation> findByCommunityIdAndStatus(Long communityId, ViolationStatus status);
}
