package roomescape.reservation.ui.dto.response;

import roomescape.reservation.domain.BookingStatus;

public record BookingStatusResponse(String id, String name) {

    public static BookingStatusResponse from(final BookingStatus status) {
        return new BookingStatusResponse(status.name(), status.getDescription());
    }
}
