package com.project.eventplan.Dto;

import java.time.LocalDate;
import java.util.List;

public record GuestInvitationDetailsResponse(
        Long guestId,
        String guestName,
        String guestEmail,
        String rsvpStatus,
        Long eventId,
        String eventName,
        LocalDate eventDate,
        String venue,
        List<String> availableStatuses
) {
}
