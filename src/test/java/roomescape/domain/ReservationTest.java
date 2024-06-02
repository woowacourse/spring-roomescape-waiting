package roomescape.domain;

import java.time.LocalDate;
import java.time.LocalTime;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import roomescape.domain.ReservationStatus.Status;
import roomescape.domain.policy.FixeDueTimePolicy;
import roomescape.domain.policy.ReservationDueTimePolicy;
import roomescape.exception.reservation.InvalidDateTimeReservationException;

class ReservationTest {

    @Test
    @DisplayName("예약 가능일 정책을 벗어난 예약은 예외를 발생시킨다 - 예외 발생")
    void validateDateTimeReservation_ShouldThrowException_WhenViolateReservationTimePolicy() {
        // given
        LocalDate date = LocalDate.of(1998, 1, 1);
        ReservationTime time = new ReservationTime(LocalTime.of(1, 1));
        Theme theme = new Theme("a", "a", "a");
        ReservationDueTimePolicy fixDueTimePolicy = new FixeDueTimePolicy();
        ReservationStatus status = new ReservationStatus(Status.RESERVED, 0);
        Member member = new Member("aa", "aa@aa.aa", "aa");

        Reservation sut = new Reservation(date, time, theme, status, member);

        // when & then
        Assertions.assertThatThrownBy(() -> sut.validateDateTimeReservation(fixDueTimePolicy))
                .isInstanceOf(InvalidDateTimeReservationException.class);
    }

    @Test
    @DisplayName("예약 가능일 정책을 벗어난 예약은 예외를 발생시킨다 - 통과")
    void validateDateTimeReservation_ShouldVerifyDueTimePolicy() {
        // given
        LocalDate date = LocalDate.of(1999, 1, 1);
        ReservationTime time = new ReservationTime(LocalTime.of(1, 1));
        Theme theme = new Theme("a", "a", "a");
        ReservationDueTimePolicy fixDueTimePolicy = new FixeDueTimePolicy();
        Member member = new Member("aa", "aa@aa.aa", "aa");
        ReservationStatus status = new ReservationStatus(Status.RESERVED, 0);

        Reservation sut = new Reservation(date, time, theme, status, member);

        // when & then
        Assertions.assertThatCode(() -> sut.validateDateTimeReservation(fixDueTimePolicy))
                .doesNotThrowAnyException();
    }
}
