package com.project.eventplan.Controller;

import com.project.eventplan.Dto.VendorRequest;
import com.project.eventplan.Dto.PageResponse;
import com.project.eventplan.Entity.User;
import com.project.eventplan.Entity.Vendor;
import com.project.eventplan.Service.VendorService;
import com.project.eventplan.Util.PaginationUtils;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/vendors")
public class VendorController {

    private final VendorService vendorService;

    public VendorController(VendorService vendorService) {
        this.vendorService = vendorService;
    }

    @PreAuthorize("hasAnyRole('ORGANIZER','TEAM_MEMBER')")
    @PostMapping
    public Vendor createVendor(@Valid @RequestBody VendorRequest request) {
        return vendorService.createVendor(toVendor(request));
    }

    @PreAuthorize("hasAnyRole('ORGANIZER','TEAM_MEMBER','VENDOR')")
    @GetMapping
    public PageResponse<Vendor> getAllVendors(@RequestParam(required = false) String query,
                                              @RequestParam(required = false) Integer page,
                                              @RequestParam(required = false) Integer size) {
        List<Vendor> vendors = vendorService.getAllVendors();
        if (query != null && !query.isBlank()) {
            String normalized = query.toLowerCase(Locale.ROOT);
            vendors = vendors.stream()
                    .filter(vendor -> contains(vendor.getVendorName(), normalized)
                            || contains(vendor.getServiceType(), normalized)
                            || contains(vendor.getContact(), normalized))
                    .toList();
        }
        return PaginationUtils.paginate(vendors, page, size);
    }

    @PreAuthorize("hasAnyRole('ORGANIZER','TEAM_MEMBER','VENDOR')")
    @GetMapping("/{id}")
    public Vendor getVendorById(@PathVariable Long id) {
        return vendorService.getVendorById(id);
    }

    @PreAuthorize("hasAnyRole('ORGANIZER','VENDOR')")
    @PutMapping("/{id}")
    public Vendor updateVendor(@PathVariable Long id,
                               @Valid @RequestBody VendorRequest request) {
        return vendorService.updateVendor(id, toVendor(request));
    }

    @PreAuthorize("hasAnyRole('ORGANIZER','VENDOR')")
    @DeleteMapping("/{id}")
    public void deleteVendor(@PathVariable Long id) {
        vendorService.deleteVendor(id);
    }

    private Vendor toVendor(VendorRequest request) {
        Vendor vendor = new Vendor();
        vendor.setVendorName(request.vendorName());
        vendor.setServiceType(request.serviceType());
        vendor.setContact(request.contact());
        if (request.userId() != null) {
            User user = new User();
            user.setUserId(request.userId());
            vendor.setUser(user);
        }
        return vendor;
    }

    private boolean contains(String value, String query) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(query);
    }
}
