package roomescape.waiting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.application.ReservationService;
import roomescape.reservation.application.dto.MemberReservationRequest;
import roomescape.waiting.application.WaitingService;
import roomescape.waiting.application.dto.WaitingIdResponse;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
public class WaitingServiceTest {

    @Autowired
    private WaitingService waitingService;

    @Autowired
    private ReservationService reservationService;

    @Test
    @DisplayName("웨이팅 정보가 들어왔을 때, 성공적으로 저장할 수 있어야 한다.")
    void add_waiting() {
        MemberReservationRequest memberReservationRequest = new MemberReservationRequest(
            LocalDate.of(2026, 5, 10),
            1L, 1L
        );
        WaitingIdResponse waitingIdResponse = waitingService.addWaiting(memberReservationRequest,
            1L);
        assertThat(waitingIdResponse.waitingId()).isEqualTo(1);
    }

    @Test
    @DisplayName("멤버는 하나의 날짜,시간,테마에 대해 예약과 대기를 동시에 할 수 없다.")
    void member_cannot_reservation_and_waiting_at_the_same_time() {
        //given
        MemberReservationRequest memberReservationRequest = new MemberReservationRequest(
            LocalDate.of(2026, 5, 10),
            1L, 1L
        );
        reservationService.addMemberReservation(memberReservationRequest, 1L);

        //when, then
        assertThatThrownBy(() -> waitingService.addWaiting(memberReservationRequest, 1L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("이미 예약이 되어있는 상태에서는, 대기할 수 없습니다.");
    }

    @Test
    @DisplayName("이미 하나의 날짜, 시간, 테마에 대해 대기가 존재한다면, 추가로 대기할 수 없다.")
    void already_waiting_then_cannot_waiting() {
        //given
        MemberReservationRequest memberReservationRequest = new MemberReservationRequest(
            LocalDate.of(2026, 5, 10),
            1L, 1L
        );
        //when, then
        waitingService.addWaiting(memberReservationRequest, 1L);
        assertThatThrownBy(() -> waitingService.addWaiting(memberReservationRequest, 1L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("이미 대기중인 상태에서는, 추가로 대기할 수 없습니다.");
    }

    @Test
    @DisplayName("본인의 대기만 삭제할 수 있어야 한다.")
    void only_delete_myself_waiting() {
        //given
        MemberReservationRequest memberReservationRequest = new MemberReservationRequest(
            LocalDate.of(2026, 5, 10),
            1L, 1L
        );

        WaitingIdResponse waitingIdResponse = waitingService.addWaiting(memberReservationRequest,
            1L);

        //when
        waitingService.cancel(1L, waitingIdResponse.waitingId());

        //then
        assertThat(waitingService.getWaitingsFromMember(1L)).isEmpty();
    }

    @Test
    @DisplayName("다른 사람이 대기 삭제를 시도할 경우, 예외가 발생해야 한다.")
    void only_delete_myself_waiting_fail_case() {
        //given
        MemberReservationRequest memberReservationRequest = new MemberReservationRequest(
            LocalDate.of(2026, 5, 10),
            1L, 1L
        );

        WaitingIdResponse waitingIdResponse = waitingService.addWaiting(memberReservationRequest,
            1L);

        //when, then
        assertThatThrownBy(() -> waitingService.cancel(2L, waitingIdResponse.waitingId()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("본인의 대기만 삭제할 수 있습니다.");
    }

}
