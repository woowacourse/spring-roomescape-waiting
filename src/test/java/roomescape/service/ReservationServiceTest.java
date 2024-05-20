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
import roomescape.domain.Role;
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
    @DisplayName("예약을 삭제한다.")
    void deleteReservation() {
        assertThatCode(() -> reservationService.deleteReservation(1L))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("자신의 예약 목록을 조회한다.")
    void getReservationsByMember() {
        final LoginMember member = new LoginMember(3L, "제제", Role.USER);
        final List<Reservation> reservationsByMember = reservationService.getReservationsByMember(member);

        final List<Reservation> expected = List.of(
                new Reservation(5L, null, null, null, null),
                new Reservation(6L, null, null, null, null)
        );

        assertThat(reservationsByMember).isEqualTo(expected);
    }

    @Test
    @DisplayName("예약 대기 목록을 조회한다.")
    void findAllWaiting() {
        //given
        //TODO 테스트 인서트 문 수정
        final LocalDate now = LocalDate.now();
        final CreateReservationRequest request1 = new CreateReservationRequest(1L, 3L, now.plusDays(3), 4L);
        final CreateReservationRequest request2 = new CreateReservationRequest(2L, 3L, now.plusDays(3), 4L);
        reservationService.addReservation(request1);
        reservationService.addReservation(request2);

        //when
        final List<Reservation> allWaiting = reservationService.findAllWaiting();
        final List<Reservation> expected = List.of(
                new Reservation(7L, null, null, null, null),
                new Reservation(8L, null, null, null, null)
        );

        //then
        assertThat(allWaiting).isEqualTo(expected);
    }
}
