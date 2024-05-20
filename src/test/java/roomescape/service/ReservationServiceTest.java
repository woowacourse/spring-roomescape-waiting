package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.controller.member.dto.LoginMember;
import roomescape.controller.reservation.dto.CreateReservationDto;
import roomescape.controller.reservation.dto.ReservationSearchCondition;
import roomescape.domain.Reservation;
import roomescape.domain.Status;
import roomescape.service.exception.DuplicateReservationException;
import roomescape.service.exception.InvalidSearchDateException;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@Sql(value = "/fixture.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class ReservationServiceTest {

    @Autowired
    private ReservationService reservationService;

    @Test
    @DisplayName("from, to 날짜가 역순이면 예외가 발생한다.")
    void searchReservationsByReversedFromToThrowsException() {
        LocalDate now = LocalDate.now();
        ReservationSearchCondition condition = new ReservationSearchCondition(1L, 1L, now,
                now.minusDays(1));
        assertThatThrownBy(() -> reservationService.searchReservations(condition))
                .isInstanceOf(InvalidSearchDateException.class);
    }

    @Test
    @DisplayName("중복된 예약을 시도하면 예외가 발생한다.")
    void addDuplicatedReservationThrowsException() {
        final LocalDate now = LocalDate.now();
        CreateReservationDto reservationDto = new CreateReservationDto(
                1L, 2L, now.plusDays(1), 3L, Status.RESERVED);

        reservationService.addReservation(reservationDto);

        assertThatThrownBy(() -> reservationService.addReservation(reservationDto))
                .isInstanceOf(DuplicateReservationException.class);
    }

    @Test
    @DisplayName("예약이 존재하지 않는데 예약 대기를 시도하면 예외가 발생한다.")
    void addReservationWaitingNotDuplicatedReservationThrowsException() {
        final LocalDate now = LocalDate.now();
        CreateReservationDto reservationDto = new CreateReservationDto(
                1L, 2L, now.plusDays(15), 3L, Status.WAITING);

        assertThatThrownBy(() -> reservationService.addReservation(reservationDto))
                .isInstanceOf(DuplicateReservationException.class);
    }

    @Test
    @DisplayName("내가 한 예약 대기가 존재하는데 예약 대기를 시도하면 예외가 발생한다.")
    void addReservationWaitingDuplicatedThrowsException() {
        final LocalDate now = LocalDate.now();
        CreateReservationDto reservationDto = new CreateReservationDto(
                1L, 2L, now.plusDays(1), 3L, Status.WAITING);
        reservationService.addReservation(new CreateReservationDto(
                1L, 2L, now.plusDays(1), 3L, Status.RESERVED));

        reservationService.addReservation(reservationDto);

        assertThatThrownBy(() -> reservationService.addReservation(reservationDto))
                .isInstanceOf(DuplicateReservationException.class);
    }

    @Test
    @DisplayName("예약을 삭제한다.")
    void deleteReservation() {
        assertThatCode(() -> reservationService.deleteReservation(1L))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("자신의 예약 목록을 조회한다.")
    void getReservationsByMember() {
        final LoginMember member = new LoginMember(3L);
        final List<Reservation> reservationsByMember = reservationService.getReservationsByMember(
                member);

        final List<Reservation> expected = List.of(
                new Reservation(5L, null, null, null, null, null),
                new Reservation(6L, null, null, null, null, null)
        );

        assertThat(reservationsByMember).isEqualTo(expected);
    }
}
