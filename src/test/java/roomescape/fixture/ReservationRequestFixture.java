package roomescape.fixture;

import roomescape.controller.dto.CreateReservationRequest;

public class ReservationRequestFixture {

    public static CreateReservationRequest create() {
        return new CreateReservationRequest(
            2L, "2060-01-01", 1L, 1L);
    }

    public static CreateReservationRequest createWithInvalidTimeId() {
        return new CreateReservationRequest(
            2L, "2060-01-01", 3L, 1L);
    }

    public static CreateReservationRequest createWithInvalidThemeId() {
        return new CreateReservationRequest(
            2L, "2060-01-01", 1L, 4L);
    }

    public static CreateReservationRequest createWithPastTime() {
        return new CreateReservationRequest(1L, "2000-01-01", 1L, 1L);
    }
}
