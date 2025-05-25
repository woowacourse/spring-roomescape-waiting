package roomescape.waiting.presentation.dto;

import roomescape.bookingslot.domain.BookingSlot;
import roomescape.waiting.domain.Waiting;

public record WaitingResponse(Long waitingId, String name, String themeName, String date, String startAt) {

    public static WaitingResponse from(final Waiting waiting) {
        BookingSlot bookingSlot = waiting.getReservation();
        return new WaitingResponse(
                waiting.getId(),
                waiting.getMember().getName(),
                bookingSlot.getTheme().getName(),
                bookingSlot.getDate().toString(),
                bookingSlot.getTime().getStartAt().toString()
        );
    }
}
