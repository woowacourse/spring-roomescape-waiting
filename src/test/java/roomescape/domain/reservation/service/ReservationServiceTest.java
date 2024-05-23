package roomescape.domain.reservation.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.ServiceTest;
import roomescape.domain.reservation.dto.ReservationAddRequest;
import roomescape.domain.reservation.dto.ReservationWaitAddRequest;
import roomescape.domain.time.dto.BookableTimeResponse;
import roomescape.domain.time.dto.BookableTimesRequest;
import roomescape.global.exception.DataConflictException;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class ReservationServiceTest extends ServiceTest {

    @Autowired
    private ReservationService reservationService;

    @DisplayName("존재 하지 않는 멤버로 예약 시 예외를 발생합니다.")
    @Test
    void should_throw_exception_when_reserve_with_non_exist_member() {
        ReservationAddRequest reservationAddRequest = new ReservationAddRequest(LocalDate.MAX, 1L, 1L, 7L);

        assertThatThrownBy(() -> reservationService.addReservation(reservationAddRequest))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("존재 하지 않는 멤버로 예약할 수 없습니다.");
    }

    @DisplayName("존재 하지 않는 테마로 예약 시 예외를 발생합니다.")
    @Test
    void should_throw_exception_when_reserve_with_non_exist_theme() {
        ReservationAddRequest reservationAddRequest = new ReservationAddRequest(LocalDate.MAX, 1L, 6L, 1L);
        assertThatThrownBy(() -> reservationService.addReservation(reservationAddRequest))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("존재 하지 않는 테마로 예약할 수 없습니다");
    }

    @DisplayName("존재하지 않는 예약시각으로 예약 시 예외가 발생합니다.")
    @Test
    void should_throw_EntityNotFoundException_when_reserve_non_exist_time() {
        ReservationAddRequest reservationAddRequest = new ReservationAddRequest(LocalDate.MAX, 6L, 1L, 1L);

        assertThatThrownBy(() -> reservationService.addReservation(reservationAddRequest))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("존재 하지 않는 예약시각으로 예약할 수 없습니다.");
    }


    @DisplayName("예약 가능/불가능 시각을 알 수 있습니다.")
    @Test
    void should_know_bookable_times() {
        List<BookableTimeResponse> bookableTimes = reservationService.findBookableTimes(
                new BookableTimesRequest(LocalDate.of(2025, 5, 14), 1L));

        assertAll(
                () -> assertThat(bookableTimes.get(0).alreadyBooked()).isFalse(),
                () -> assertThat(bookableTimes.get(1).alreadyBooked()).isTrue(),
                () -> assertThat(bookableTimes.get(2).alreadyBooked()).isFalse(),
                () -> assertThat(bookableTimes.get(3).alreadyBooked()).isFalse(),
                () -> assertThat(bookableTimes.get(4).alreadyBooked()).isFalse()
        );
    }

    @DisplayName("예약 날짜와 예약시각 그리고 테마 아이디가 같은 예약이 있는 경우 예약 생성에 예외를 발생합니다.")
    @Test
    void should_throw_DataConflictException_when_reserve_date_and_time_and_theme_and_status_duplicated() {
        ReservationAddRequest firstReservationAddRequest = new ReservationAddRequest(LocalDate.MAX, 1L, 1L, 1L);
        ReservationAddRequest secondReservationAddRequest = new ReservationAddRequest(LocalDate.MAX, 1L, 1L, 2L);

        reservationService.addReservation(firstReservationAddRequest);

        assertThatThrownBy(() -> reservationService.addReservation(secondReservationAddRequest))
                .isInstanceOf(DataConflictException.class)
                .hasMessage("예약 날짜와 예약시간 그리고 테마가 겹치는 예약이 있으면 예약을 할 수 없습니다.");
    }

    // TODO: 멤버와 예약 날짜 그리고 예약시각, 테마 아이디가 같은 예약대기가 있는 경우 예약 생성에 예외를 발생하는 테스트 필요 (예약 삭제 메서드 필요)

    @DisplayName("예약 날짜와 예약시각 그리고 테마 아이디가 같은 예약이 없는 경우 예약대기 생성에 예외를 발생합니다.")
    @Test
    void should_throw_DataConflictException_when_reserve_wait_date_and_time_and_theme_and_status_not_exist() {
        ReservationWaitAddRequest reservationWaitAddRequest = new ReservationWaitAddRequest(LocalDate.MAX, 1L, 1L, 1L);

        assertThatThrownBy(() -> reservationService.addReservationWait(reservationWaitAddRequest))
                .isInstanceOf(DataConflictException.class)
                .hasMessage("예약 날짜와 예약시간 그리고 테마가 겹치는 예약이 없으면 예약대기를 할 수 없습니다.");
    }

    @DisplayName("멤버와 예약 날짜 그리고 예약시각, 테마 아이디가 같은 예약대기가 있는 경우 예약대기 생성에 예외를 발생합니다.")
    @Test
    void should_throw_DataConflictException_when_reserve_wait_member_and_date_and_time_and_theme_and_status_duplicated() {
        ReservationAddRequest reservationAddRequest = new ReservationAddRequest(LocalDate.MAX, 1L, 1L, 2L);
        ReservationWaitAddRequest reservationWaitAddRequest = new ReservationWaitAddRequest(LocalDate.MAX, 1L, 1L, 1L);
        reservationService.addReservation(reservationAddRequest);

        reservationService.addReservationWait(reservationWaitAddRequest);

        assertThatThrownBy(() -> reservationService.addReservationWait(reservationWaitAddRequest))
                .isInstanceOf(DataConflictException.class)
                .hasMessage("멤버와 예약 날짜 그리고 예약시간, 테마가 겹치는 예약 또는 예약대기가 있으면 예약대기를 할 수 없습니다.");
    }

    @DisplayName("없는 id의 예약을 삭제하면 예외를 발생합니다.")
    @Test
    void should_throw_EntityNotFoundException_when_remove_reservation_with_non_exist_id() {
        assertThatThrownBy(() -> reservationService.removeReservation(21L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("해당 id를 가진 예약이 존재하지 않습니다.");
    }
}
