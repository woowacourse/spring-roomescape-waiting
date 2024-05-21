package roomescape.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import roomescape.domain.policy.FixeDueTimePolicy;
import roomescape.domain.policy.ReservationDueTimePolicy;
import roomescape.exception.reservation.DuplicatedReservationException;
import roomescape.exception.reservation.InvalidDateTimeReservationException;

class ReservationTest {
    @Test
    @DisplayName("중복된 예약이 있는지 검증한다 - 예외 발생")
    void validateDuplicateReservation_ShouldThrowException_WhenHasDuplicateReservations() {
        // given
        ReservationTime time = new ReservationTime(LocalTime.of(1, 1));
        Theme theme = new Theme("a", "a", "a");
        Member member = new Member("a", "a", "a");
        LocalDate date = LocalDate.of(2023, 1, 1);
        List<Reservation> reservations = List.of(new Reservation(date, time, theme, member));
        Reservation sut = new Reservation(date, time, theme, member);

        // when & them
        Assertions.assertThatThrownBy(() -> sut.validateDuplicateDateTime(reservations))
                .isInstanceOf(DuplicatedReservationException.class);
    }

    @Test
    @DisplayName("중복된 예약이 있는 검증한다 - 통과")
    void validateDuplicateReservation_ShouldVerifyDuplicateReservations() {
        // given
        LocalDate date = LocalDate.of(2023, 1, 1);
        ReservationTime time = new ReservationTime(LocalTime.of(1, 1));
        Theme theme = new Theme("a", "a", "a");
        Member member = new Member("a", "a", "a");

        Reservation sut = new Reservation(date, time, theme, member);

        // when & them
        Assertions.assertThatCode(() -> sut.validateDuplicateDateTime(List.of()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("예약 가능일 정책을 벗어난 예약은 예외를 발생시킨다 - 예외 발생")
    void validateDateTimeReservation_ShouldThrowException_WhenViolateReservationTimePolicy() {
        // given
        LocalDate date = LocalDate.of(1998, 1, 1);
        ReservationTime time = new ReservationTime(LocalTime.of(1, 1));
        Theme theme = new Theme("a", "a", "a");
        Member member = new Member("a", "a", "a");
        ReservationDueTimePolicy fixDueTimePolicy = new FixeDueTimePolicy();

        Reservation sut = new Reservation(date, time, theme, member);

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
        Member member = new Member("a", "a", "a");
        ReservationDueTimePolicy fixDueTimePolicy = new FixeDueTimePolicy();

        Reservation sut = new Reservation(date, time, theme, member);

        // when & then
        Assertions.assertThatCode(() -> sut.validateDateTimeReservation(fixDueTimePolicy))
                .doesNotThrowAnyException();
    }
}
