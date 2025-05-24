package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThatCode;

import java.time.LocalDateTime;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import roomescape.domain.member.Email;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRole;
import roomescape.infrastructure.error.exception.ReservationException;

class ReservationTest {

    @ParameterizedTest
    @CsvSource({"2025-04-23T12:30, 2025-04-22T12:30", "2025-04-23T12:30, 2025-04-23T12:00"})
    void 지난_날짜에_대한_예약이라면_예외가_발생한다(LocalDateTime currentDateTime, LocalDateTime reservationDateTime) {
        // given
        Reservation reservation = new Reservation(
                new Member(1L, "test1", new Email("email1@gmail.com"), "password", MemberRole.NORMAL),
                reservationDateTime.toLocalDate(),
                new ReservationTime(1L, reservationDateTime.toLocalTime()),
                new Theme(1L, "test", "description", "thumbnail")
        );

        // when
        // then
        assertThatCode(() -> reservation.validateReservable(currentDateTime))
                .isInstanceOf(ReservationException.class)
                .hasMessage("지난 날짜와 시간에 대한 예약은 불가능합니다.");
    }

    @ParameterizedTest
    @CsvSource({"2025-04-23T12:30, 2025-04-23T12:30", "2025-04-23T12:30, 2025-04-23T12:39"})
    void 예약일이_오늘인_경우_예약_시간까지_10분도_남지_않았다면_예외가_발생한다(LocalDateTime currentDateTime, LocalDateTime reservationDateTime) {
        // given
        Reservation reservation = new Reservation(
                new Member(1L, "test1", new Email("email1@gmail.com"), "password", MemberRole.NORMAL),
                reservationDateTime.toLocalDate(),
                new ReservationTime(1L, reservationDateTime.toLocalTime()),
                new Theme(1L, "test", "description", "thumbnail")
        );

        // when
        // then
        assertThatCode(() -> reservation.validateReservable(currentDateTime))
                .isInstanceOf(ReservationException.class)
                .hasMessage("예약 시간까지 10분도 남지 않아 예약이 불가능합니다.");
    }
}
