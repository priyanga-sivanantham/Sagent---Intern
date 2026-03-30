package com.project.eventplan.Repository;

import com.project.eventplan.Entity.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VendorRepository extends JpaRepository<Vendor, Long> {
    List<Vendor> findByContact(String contact);
    List<Vendor> findByUserEmail(String email);
    Optional<Vendor> findByVendorIdAndUserEmail(Long vendorId, String email);
}
