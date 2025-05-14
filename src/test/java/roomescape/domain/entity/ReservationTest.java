package roomescape.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.domain.reservation.model.dto.ReservationDetails;
import roomescape.domain.reservation.model.entity.Reservation;
import roomescape.domain.reservation.model.entity.ReservationTheme;
import roomescape.domain.reservation.model.entity.ReservationTime;
import roomescape.domain.reservation.model.exception.ReservationException.InvalidReservationTimeException;

class ReservationTest {

    @DisplayName("예약을 생성할 수 있다")
    @Test
    void createFutureReservationSuccess() {
        //given
        ReservationDetails details = new ReservationDetails(
                "웨이드",
                1L,
                LocalDate.now().plusDays(10),
                new ReservationTime(LocalTime.of(10, 0)),
                new ReservationTheme("테마 이름", "테마 설명", "테마 url")
        );

        //when
        Reservation reservation = Reservation.createFutureReservation(details);

        //then
        assertThat(reservation.getName()).isEqualTo("웨이드");
        assertThat(reservation.getDate()).isEqualTo(details.date());
    }

    @DisplayName("예약 생성시 예약 시간이 과거 시간이면 예외를 발생시킨다")
    @Test
    void createFutureReservationExceptionIfPastGetTime() {
        //given
        ReservationDetails details = new ReservationDetails(
                "홍길동",
                1L,
                LocalDate.now().minusDays(1),
                new ReservationTime(LocalTime.of(10, 0)),
                new ReservationTheme("테마 이름", "테마 설명", "테마 url")
        );

        //when & then
        assertThatThrownBy(() -> Reservation.createFutureReservation(details))
                .isInstanceOf(InvalidReservationTimeException.class);
    }

    @DisplayName("예약 생성시 예약자 명이 null이거나 빈값이라면 예외를 발생시킨다")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void getNameNullException(String value) {
        assertThatThrownBy(() -> new Reservation(
                value,
                LocalDate.now().plusDays(1),
                new ReservationTime(LocalTime.of(10, 0)),
                new ReservationTheme("이름", "테스트", "url")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("예약 생성시 날짜가 null이라면 예외를 발생시킨다"
            + "")
    @Test
    void getDateNullException() {
        assertThatThrownBy(() -> new Reservation(
                "이름",
                null,
                new ReservationTime(LocalTime.of(10, 0)),
                new ReservationTheme("이름", "테스트", "url")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("id를 할당한 새로운 예약 객체를 얻을 수 있다")
    @Test
    void assignGetIdSuccess() {
        //given
        Reservation original = new Reservation(
                "웨이드",
                LocalDate.now().plusDays(1),
                new ReservationTime(1L, LocalTime.of(10, 0)),
                new ReservationTheme("이름", "테마", "url")
        );

        Long assignedId = 1L;

        //when
        Reservation reservationWithId = original.assignId(assignedId);

        //then
        assertThat(reservationWithId.getId()).isEqualTo(assignedId);
        assertThat(reservationWithId.getName()).isEqualTo(original.getName());
    }

    @DisplayName("할당한 id가 null이라면 예외를 발생시킨다")
    @Test
    void assignGetIdNull() {
        //given
        Reservation original = new Reservation(
                "웨이드",
                LocalDate.now().plusDays(1),
                new ReservationTime(1L, LocalTime.of(10, 0)),
                new ReservationTheme("이름", "테마", "url")
        );

        //when & then
        assertThatThrownBy(() -> original.assignId(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
