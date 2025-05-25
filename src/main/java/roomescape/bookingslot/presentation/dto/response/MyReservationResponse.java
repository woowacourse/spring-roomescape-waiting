package roomescape.bookingslot.presentation.dto.response;

import roomescape.bookingslot.domain.BookingSlot;
import roomescape.waiting.domain.Waiting;

public record MyReservationResponse(Long reservationId,
                                    String theme,
                                    String date,
                                    String time,
                                    String status) {

    public static MyReservationResponse from(final Waiting waiting) {
        BookingSlot bookingSlot = waiting.getReservation();
        String waitingMessage = String.format(waiting.getWaitingStatus().getTitle(), bookingSlot.findRank(waiting));
        return new MyReservationResponse(bookingSlot.getId(), bookingSlot.getTheme().getName(),
                bookingSlot.getDate().toString(),
                bookingSlot.getTime().getStartAt().toString(), waitingMessage);
    }
}
