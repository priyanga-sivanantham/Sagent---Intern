package com.project.eventplan.Dto;

import java.time.LocalDate;

public record FeedbackLinkDetailsResponse(
        Long guestId,
        String guestName,
        String guestEmail,
        Long eventId,
        String eventName,
        LocalDate eventDate,
        String venue
) {
}
