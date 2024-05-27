package roomescape.fixture;

import static roomescape.fixture.MemberFixture.DEFAULT_MEMBER;
import static roomescape.fixture.ReservationTimeFixture.DEFAULT_TIME;
import static roomescape.fixture.ThemeFixture.DEFAULT_THEME;

import java.time.LocalDate;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.dto.ReservationRequest;
import roomescape.dto.ReservationResponse;

public class ReservationFixture {
    public static final Reservation DEFAULT_RESERVATION = Reservation.builder()
            .id(1L)
            .member(DEFAULT_MEMBER)
            .date(LocalDate.now().plusDays(1))
            .time(DEFAULT_TIME)
            .theme(DEFAULT_THEME)
            .build();

    public static final ReservationRequest DEFAULT_REQUEST = new ReservationRequest(
            DEFAULT_RESERVATION.getDate(),
            DEFAULT_MEMBER.getId(),
            DEFAULT_TIME.getId(),
            DEFAULT_THEME.getId()
    );

    public static final ReservationResponse DEFAULT_RESPONSE = new ReservationResponse(
            1L,
            DEFAULT_MEMBER.getName(),
            DEFAULT_RESERVATION.getDate(),
            ReservationTimeFixture.DEFAULT_RESPONSE,
            ThemeFixture.DEFAULT_RESPONSE,
            ReservationStatus.APPROVED
    );
}
