package roomescape.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import roomescape.controller.member.dto.LoginMember;
import roomescape.controller.reservation.dto.ReservationSearchCondition;
import roomescape.domain.Reservation;
import roomescape.domain.Role;
import roomescape.repository.dto.ReservationRankResponse;
import roomescape.service.exception.InvalidSearchDateException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
class ReservationServiceTest {

    @Autowired
    private ReservationService reservationService;

    @Test
    @DisplayName("from, to 날짜가 역순이면 예외가 발생한다.")
    void searchReservationsByReversedFromToThrowsException() {
        LocalDate now = LocalDate.now();
        ReservationSearchCondition condition = new ReservationSearchCondition(1L, 1L, now, now.minusDays(1));
        assertThatThrownBy(() -> reservationService.searchReservations(condition))
                .isInstanceOf(InvalidSearchDateException.class);
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
        final LoginMember member = new LoginMember(3L, "제제", Role.USER);
        final List<ReservationRankResponse> reservationsByMember = reservationService.getMyReservation(member);
        final LocalDate date = LocalDate.now();

        final List<ReservationRankResponse> expected = List.of(
                new ReservationRankResponse(5L, "가을", date.minusDays(7), LocalTime.of(15, 0), 1),
                new ReservationRankResponse(6L, "가을", date.plusDays(3), LocalTime.of(18, 0), 1),
                new ReservationRankResponse(8L, "가을", date.plusDays(4), LocalTime.of(18, 0), 2)
        );

        assertThat(reservationsByMember).isEqualTo(expected);
    }

    @Test
    @DisplayName("예약 대기 목록을 조회한다.")
    void findAllWaiting() {
        final List<Reservation> allWaiting = reservationService.findAllWaiting();
        final List<Reservation> expected = List.of(new Reservation(8L, null, null, null, null));

        //then
        assertThat(allWaiting).isEqualTo(expected);
    }
}
