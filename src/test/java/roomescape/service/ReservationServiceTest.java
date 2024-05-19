package roomescape.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import roomescape.controller.member.dto.LoginMember;
import roomescape.controller.reservation.dto.CreateReservationRequest;
import roomescape.controller.reservation.dto.ReservationSearchCondition;
import roomescape.domain.Reservation;
import roomescape.domain.WaitingRank;
import roomescape.service.exception.InvalidSearchDateException;

import java.time.LocalDate;
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
    @DisplayName("같은 시간으로 예약하면 예약 대기가 된다.")
    void addDuplicatedReservationThrowsException() {
        final LocalDate now = LocalDate.now();
        CreateReservationRequest request = new CreateReservationRequest(1L, 2L, now.plusDays(1), 3L);

        final Reservation reserved = reservationService.addReservation(request);
        final Reservation waited = reservationService.addReservation(request);

        assertThat(reserved.getRank()).isEqualTo(new WaitingRank(0L));
        assertThat(waited.getRank()).isEqualTo(new WaitingRank(1L));
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
        final LoginMember member = new LoginMember(3L, "제제", "USER");
        final List<Reservation> reservationsByMember = reservationService.getReservationsByMember(member);

        final List<Reservation> expected = List.of(
                new Reservation(5L, null, null, null, null, WaitingRank.createFirst()),
                new Reservation(6L, null, null, null, null, WaitingRank.createFirst())
        );

        assertThat(reservationsByMember).isEqualTo(expected);
    }
}
