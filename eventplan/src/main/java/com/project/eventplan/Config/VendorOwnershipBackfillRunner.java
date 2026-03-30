package com.project.eventplan.Config;

import com.project.eventplan.Entity.User;
import com.project.eventplan.Entity.Vendor;
import com.project.eventplan.Repository.UserRepository;
import com.project.eventplan.Repository.VendorRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class VendorOwnershipBackfillRunner implements ApplicationRunner {

    private final VendorRepository vendorRepository;
    private final UserRepository userRepository;

    public VendorOwnershipBackfillRunner(VendorRepository vendorRepository,
                                         UserRepository userRepository) {
        this.vendorRepository = vendorRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<Vendor> vendors = vendorRepository.findAll();

        for (Vendor vendor : vendors) {
            if (vendor.getUser() != null || vendor.getContact() == null || vendor.getContact().isBlank()) {
                continue;
            }

            List<User> users = userRepository.findByPhone(vendor.getContact());
            if (users.size() == 1) {
                vendor.setUser(users.getFirst());
                vendorRepository.save(vendor);
            }
        }
    }
}
