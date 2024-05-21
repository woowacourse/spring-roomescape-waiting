package roomescape.fixture;

import static roomescape.fixture.MemberFixture.DEFAULT_ADMIN;
import static roomescape.fixture.ReservationFixture.DEFAULT_RESERVATION;

import roomescape.domain.ReservationWaiting;
import roomescape.dto.ReservationRequest;
import roomescape.dto.ReservationWaitingResponse;

public class ReservationWaitingFixture {
    public static final ReservationWaiting DEFAULT_WAITING = new ReservationWaiting(1L, DEFAULT_RESERVATION,
            DEFAULT_ADMIN);
    public static final ReservationRequest DEFAULT_REQUEST = new ReservationRequest(DEFAULT_RESERVATION.getDate(),
            DEFAULT_ADMIN.getId(), DEFAULT_RESERVATION.getReservationTime().getId(),
            DEFAULT_RESERVATION.getTheme().getId());

    public static final ReservationWaitingResponse DEFAULT_RESPONSE = new ReservationWaitingResponse(
            DEFAULT_WAITING.getId(), DEFAULT_WAITING.getWaitingMember().getName(),
            DEFAULT_WAITING.getReservation().getDate(), ReservationTimeFixture.DEFAULT_RESPONSE,
            ThemeFixture.DEFAULT_RESPONSE, 1);
}
