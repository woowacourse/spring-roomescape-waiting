package roomescape.reservation.ui.dto.response;

import roomescape.reservation.domain.BookingState;

public record BookingStateResponse(String id, String name) {

    public static BookingStateResponse from(final BookingState state) {
        return new BookingStateResponse(state.name(), state.getDescription());
    }
}
