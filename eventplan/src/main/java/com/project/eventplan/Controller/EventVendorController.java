package com.project.eventplan.Controller;

import com.project.eventplan.Entity.EventVendor;
import com.project.eventplan.Service.EventVendorService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/eventvendors")
public class EventVendorController {

    private final EventVendorService eventVendorService;

    public EventVendorController(EventVendorService eventVendorService){
        this.eventVendorService = eventVendorService;
    }

    // ASSIGN VENDOR TO EVENT
    @PreAuthorize("hasRole('ORGANIZER')")
    @PostMapping("/assign")
    public EventVendor assignVendor(@RequestParam Long eventId,
                                    @RequestParam Long vendorId,
                                    @RequestParam String serviceDetails){

        return eventVendorService.assignVendor(eventId,vendorId,serviceDetails);
    }

    // GET ALL ASSIGNMENTS
    @PreAuthorize("hasAnyRole('ORGANIZER','TEAM_MEMBER','VENDOR')")
    @GetMapping
    public List<EventVendor> getAllAssignments(){
        return eventVendorService.getAllAssignments();
    }

    // DELETE ASSIGNMENT
    @PreAuthorize("hasRole('ORGANIZER')")
    @DeleteMapping("/{id}")
    public void deleteAssignment(@PathVariable Long id){
        eventVendorService.deleteAssignment(id);
    }
}
