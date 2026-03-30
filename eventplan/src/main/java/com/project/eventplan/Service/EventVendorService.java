package com.project.eventplan.Service;

import com.project.eventplan.Config.SecurityUtil;
import com.project.eventplan.Entity.Event;
import com.project.eventplan.Entity.EventVendor;
import com.project.eventplan.Entity.Vendor;
import com.project.eventplan.Entity.User;
import com.project.eventplan.Exception.BadRequestException;
import com.project.eventplan.Exception.ResourceNotFoundException;
import com.project.eventplan.Repository.EventRepository;
import com.project.eventplan.Repository.EventVendorRepository;
import com.project.eventplan.Repository.VendorRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventVendorService {

    private final EventVendorRepository eventVendorRepository;
    private final EventRepository eventRepository;
    private final VendorRepository vendorRepository;
    private final SecurityUtil securityUtil;

    public EventVendorService(EventVendorRepository eventVendorRepository,
                              EventRepository eventRepository,
                              VendorRepository vendorRepository,
                              SecurityUtil securityUtil) {
        this.eventVendorRepository = eventVendorRepository;
        this.eventRepository = eventRepository;
        this.vendorRepository = vendorRepository;
        this.securityUtil = securityUtil;
    }

    public EventVendor assignVendor(Long eventId, Long vendorId, String serviceDetails) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));

        String currentUserEmail = securityUtil.getCurrentUserEmail();
        if (!event.getOrganizer().getEmail().equals(currentUserEmail)) {
            throw new AccessDeniedException("Not your event");
        }

        User user = vendor.getUser();
        if (user == null) {
            throw new BadRequestException("Vendor is not linked to a user");
        }

        if (!user.getRole().name().equals("VENDOR")) {
            throw new BadRequestException("Linked user is not a vendor");
        }

        EventVendor eventVendor = new EventVendor();
        eventVendor.setEvent(event);
        eventVendor.setUser(user);
        eventVendor.setServiceDetails(serviceDetails);
        return eventVendorRepository.save(eventVendor);
    }

    public List<EventVendor> getAllAssignments() {
        String email = securityUtil.getCurrentUserEmail();

        return eventVendorRepository.findAll().stream()
                .filter(assignment -> assignment.getEvent() != null
                        && assignment.getEvent().getOrganizer() != null
                        && (email.equals(assignment.getEvent().getOrganizer().getEmail())
                        || (assignment.getUser() != null && email.equals(assignment.getUser().getEmail()))))
                .collect(Collectors.toList());
    }

    public void deleteAssignment(Long id) {
        EventVendor ev = eventVendorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));

        String currentUserEmail = securityUtil.getCurrentUserEmail();
        if (!ev.getEvent().getOrganizer().getEmail().equals(currentUserEmail)) {
            throw new AccessDeniedException("Not your event");
        }

        eventVendorRepository.delete(ev);
    }
}
