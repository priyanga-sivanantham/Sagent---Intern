package com.project.eventplan.Service;

import com.project.eventplan.Config.SecurityUtil;
import com.project.eventplan.Entity.Enums.Role;
import com.project.eventplan.Entity.User;
import com.project.eventplan.Entity.Vendor;
import com.project.eventplan.Exception.ResourceNotFoundException;
import com.project.eventplan.Repository.UserRepository;
import com.project.eventplan.Repository.VendorRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VendorService {

    private final VendorRepository vendorRepository;
    private final UserRepository userRepository;
    private final SecurityUtil securityUtil;

    public VendorService(VendorRepository vendorRepository,
                         UserRepository userRepository,
                         SecurityUtil securityUtil) {
        this.vendorRepository = vendorRepository;
        this.userRepository = userRepository;
        this.securityUtil = securityUtil;
    }

    public Vendor createVendor(Vendor vendor) {
        User currentUser = getCurrentUser();

        if (currentUser.getRole() == Role.VENDOR) {
            vendor.setUser(currentUser);
            if (vendor.getContact() == null || vendor.getContact().isBlank()) {
                vendor.setContact(currentUser.getPhone());
            }
        } else if (vendor.getUser() != null && vendor.getUser().getUserId() != null) {
            User linkedUser = userRepository.findById(vendor.getUser().getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            vendor.setUser(linkedUser);
        } else {
            vendor.setUser(null);
        }

        return vendorRepository.save(vendor);
    }

    public List<Vendor> getAllVendors() {
        User currentUser = getCurrentUser();

        if (currentUser.getRole() == Role.VENDOR) {
            List<Vendor> linkedVendors = vendorRepository.findByUserEmail(currentUser.getEmail());
            if (!linkedVendors.isEmpty()) {
                return linkedVendors;
            }

            if (currentUser.getPhone() == null) {
                return List.of();
            }

            return vendorRepository.findByContact(currentUser.getPhone());
        }

        return vendorRepository.findAll();
    }

    public Vendor getVendorById(Long id) {
        Vendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));
        enforceVendorAccess(getCurrentUser(), vendor);
        return vendor;
    }

    public Vendor updateVendor(Long id, Vendor vendor) {
        Vendor existing = vendorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));

        User currentUser = getCurrentUser();
        enforceVendorAccess(currentUser, existing);

        existing.setVendorName(vendor.getVendorName());
        existing.setServiceType(vendor.getServiceType());
        existing.setContact(vendor.getContact());

        if (currentUser.getRole() == Role.VENDOR) {
            existing.setUser(currentUser);
            if (existing.getContact() == null || existing.getContact().isBlank()) {
                existing.setContact(currentUser.getPhone());
            }
        } else if (vendor.getUser() != null && vendor.getUser().getUserId() != null) {
            User linkedUser = userRepository.findById(vendor.getUser().getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            existing.setUser(linkedUser);
        }

        return vendorRepository.save(existing);
    }

    public void deleteVendor(Long id) {
        Vendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));
        enforceVendorAccess(getCurrentUser(), vendor);
        vendorRepository.deleteById(id);
    }

    private User getCurrentUser() {
        String email = securityUtil.getCurrentUserEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private void enforceVendorAccess(User currentUser, Vendor vendor) {
        if (currentUser.getRole() == Role.ORGANIZER || currentUser.getRole() == Role.TEAM_MEMBER) {
            return;
        }

        if (currentUser.getRole() == Role.VENDOR) {
            if (vendor.getUser() != null && currentUser.getEmail().equals(vendor.getUser().getEmail())) {
                return;
            }

            if (vendor.getUser() == null
                    && currentUser.getPhone() != null
                    && currentUser.getPhone().equals(vendor.getContact())) {
                return;
            }
        }

        throw new AccessDeniedException("Access denied");
    }
}
