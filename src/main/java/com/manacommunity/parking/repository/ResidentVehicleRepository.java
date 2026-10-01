package com.manacommunity.parking.repository;

import com.manacommunity.parking.model.ResidentVehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResidentVehicleRepository extends JpaRepository<ResidentVehicle, Long> {
    Optional<ResidentVehicle> findByLicensePlate(String licensePlate);
    List<ResidentVehicle> findByResidentIdAndActiveTrue(Long residentId);
    List<ResidentVehicle> findByCommunityIdAndActiveTrue(Long communityId);
}
