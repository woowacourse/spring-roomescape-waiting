package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.TestFixture.fixedClockAt;

import java.time.Clock;
import java.time.LocalDateTime;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import roomescape.TestFixture;
import roomescape.exception.UnAvailableReservationException;

class ReservationPolicyTest {

    private static final String PAST_RESERVATION_MESSAGE = "지난 날짜와 시간에 대한 예약은 불가능합니다.";
    private static final String TOO_SOON_RESERVATION_MESSAGE = "예약 시간까지 10분도 남지 않아 예약이 불가합니다.";
    private static final String DUPLICATE_RESERVATION_MESSAGE = "테마에 대해 날짜와 시간이 중복된 예약이 존재합니다.";

    @ParameterizedTest
    @CsvSource({
            "2025-04-23T12:00, 2025-04-23T11:59",
            "2025-04-23T12:00, 2025-04-22T12:00"
    })
    void 지난_날짜_예약은_불가능하다(LocalDateTime fixedNowStr, LocalDateTime reservationDateTimeStr) {
        // given
        Clock clock = fixedClockAt(fixedNowStr);
        Reservation reservation = reservationAt(reservationDateTimeStr);
        ReservationPolicy policy = new ReservationPolicy(clock);

        // when & then
        assertThatThrownBy(() -> policy.validateReservationAvailable(reservation, false))
                .isInstanceOf(UnAvailableReservationException.class)
                .hasMessage(PAST_RESERVATION_MESSAGE);
    }

    @ParameterizedTest
    @CsvSource({
            "2025-04-23T12:00, 2025-04-23T12:00",
            "2025-04-23T12:00, 2025-04-23T12:09"
    })
    void 임박한_예약은_불가능하다(LocalDateTime fixedNowStr, LocalDateTime reservationDateTimeStr) {
        // given
        Clock clock = fixedClockAt(fixedNowStr);
        Reservation reservation = reservationAt(reservationDateTimeStr);
        ReservationPolicy policy = new ReservationPolicy(clock);

        // when & then
        assertThatThrownBy(() -> policy.validateReservationAvailable(reservation, false))
                .isInstanceOf(UnAvailableReservationException.class)
                .hasMessage(TOO_SOON_RESERVATION_MESSAGE);
    }

    @ParameterizedTest
    @CsvSource({"2025-04-23T12:00, 2025-04-23T13:00"})
    void 중복된_예약은_불가능하다(LocalDateTime fixedNowStr, LocalDateTime dateTime) {
        // given
        Clock clock = fixedClockAt(fixedNowStr);
        Reservation reservation = reservationAt(dateTime);
        ReservationPolicy policy = new ReservationPolicy(clock);

        // when & then
        assertThatThrownBy(() -> policy.validateReservationAvailable(reservation, true))
                .isInstanceOf(UnAvailableReservationException.class)
                .hasMessage(DUPLICATE_RESERVATION_MESSAGE);
    }

    private Reservation reservationAt(LocalDateTime dateTime) {
        Member member = TestFixture.createDefaultMember();
        Theme theme = TestFixture.createDefaultTheme();

        ReservationTime reservationTime = TestFixture.createTimeFrom(dateTime.toLocalTime());

        return TestFixture.createNewReservation(member, dateTime.toLocalDate(), reservationTime, theme);
    }
}
