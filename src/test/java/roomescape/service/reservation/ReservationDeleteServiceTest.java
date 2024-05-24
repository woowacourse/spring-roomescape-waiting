package roomescape.service.reservation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.Role;
import roomescape.exception.AuthenticationException;
import roomescape.repository.ReservationRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationDeleteServiceTest {

    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ReservationDeleteService reservationDeleteService;

    @Test
    @DisplayName("예약 취소가 발생할 시 예약 대기가 승인된다.")
    void deleteReservedTime_changeWaitingToReserved() {
        Reservation beforeUpdatedReservation = reservationRepository.findById(2L).get();
        assertThat(beforeUpdatedReservation.isReserved()).isFalse();

        Member member = new Member(1L, "testUser", "user@naver.com", "1234", Role.MEMBER);
        reservationDeleteService.deleteReservation(1L, member);
        Reservation updatedReservation = reservationRepository.findById(2L).get();
        assertThat(updatedReservation.isReserved()).isTrue();
    }

    @Test
    @DisplayName("본인의 예약이 아닌 경우 삭제할 수 없다.")
    void deleteOtherReservation_Failure() {
        Reservation reservation = reservationRepository.findById(1L).get();
        Member member = new Member(3L, "testUser2", "user2@naver.com", "1234", Role.MEMBER);

        assertThatThrownBy(() -> reservationDeleteService.deleteReservation(1L, member))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("본인의 예약만 삭제할 수 있습니다.");
    }
}
