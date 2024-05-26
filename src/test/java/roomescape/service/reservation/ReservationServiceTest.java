package roomescape.service.reservation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationWaitingWithRank;
import roomescape.domain.Role;
import roomescape.exception.AuthenticationException;
import roomescape.repository.ReservationRepository;
import roomescape.service.dto.request.ReservationSaveRequest;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class ReservationServiceTest {

    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ReservationService reservationService;

    @Test
    @DisplayName("예약 가능한 시간인 경우 성공한다.")
    void checkDuplicateReservationTime_Success() {
        ReservationSaveRequest request = new ReservationSaveRequest(
                LocalDate.now().plusDays(1L), 2L, 2L, ReservationStatus.RESERVED);
        Member member = new Member(1L, "capy", "test@naver.com", "1234", Role.MEMBER);

        assertThatCode(() -> reservationService.createReservation(
                        request,
                        member
                )
        )
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("이미 예약된 시간인 경우 예외가 발생한다.")
    void checkDuplicateReservationTime_Failure() {
        ReservationSaveRequest request = new ReservationSaveRequest(
                LocalDate.now().plusDays(1L), 1L, 1L, ReservationStatus.RESERVED);
        Member member = new Member("capy", "abc@gmail.com", "1234", Role.MEMBER);

        assertThatThrownBy(() -> reservationService.createReservation(
                        request,
                        member
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 시간에 이미 예약된 테마입니다.");
    }

    @Test
    @DisplayName("지나간 날짜와 시간에 대한 예약 생성시 예외가 발생한다.")
    void checkReservationDateTimeIsFuture_Failure() {
        ReservationSaveRequest request = new ReservationSaveRequest(
                LocalDate.now().minusDays(1L), 2L, 2L, ReservationStatus.RESERVED);
        Member member = new Member("capy", "abc@gmail.com", "1234", Role.MEMBER);

        assertThatThrownBy(() -> reservationService.createReservation(
                        request,
                        member
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("지나간 날짜와 시간에 대한 예약 생성은 불가능합니다.");
    }

    @Test
    @DisplayName("사용자의 예약대기가 몇번째인지 구한다.")
    void findWaitingRank() {
        List<ReservationWaitingWithRank> waitingWithRanks1 =
                reservationService.findMemberReservations(2L);
        List<ReservationWaitingWithRank> waitingWithRanks2 =
                reservationService.findMemberReservations(3L);
        assertAll(
                () -> waitingWithRanks1.get(0).getRank().equals(1),
                () -> waitingWithRanks1.get(1).getRank().equals(1),
                () -> waitingWithRanks2.get(0).getRank().equals(2),
                () -> waitingWithRanks2.get(1).getRank().equals(2)
        );
    }

    @Test
    @DisplayName("예약 취소가 발생할 시 예약 대기가 승인된다.")
    void deleteReservedTime_changeWaitingToReserved() {
        Reservation beforeUpdatedReservation = reservationRepository.findById(2L).get();
        assertThat(beforeUpdatedReservation.isReserved()).isFalse();

        Member member = new Member(1L, "testUser", "user@naver.com", "1234", Role.MEMBER);
        reservationService.deleteReservation(1L, member);
        Reservation updatedReservation = reservationRepository.findById(2L).get();
        assertThat(updatedReservation.isReserved()).isTrue();
    }

    @Test
    @DisplayName("본인의 예약이 아닌 경우 삭제할 수 없다.")
    void deleteOtherReservation_Failure() {
        Reservation reservation = reservationRepository.findById(1L).get();
        Member member = new Member(3L, "testUser2", "user2@naver.com", "1234", Role.MEMBER);

        assertThatThrownBy(() -> reservationService.deleteReservation(1L, member))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("본인의 예약만 삭제할 수 있습니다.");
    }
}
