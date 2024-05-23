package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.domain.member.Member;
import roomescape.exception.reservationwaiting.DuplicatedReservationWaitingException;
import roomescape.service.member.MemberService;
import roomescape.service.reservationwaiting.ReservationWaitingService;
import roomescape.service.reservationwaiting.dto.ReservationWaitingRequest;

public class ReservationWaitingServiceTest extends ServiceTest {
    @Autowired
    private ReservationWaitingService reservationWaitingService;

    @Autowired
    private MemberService memberService;

    @Nested
    @DisplayName("예약 대기 추가")
    class SaveReservationWaiting {
        @Test
        void 예약_대기를_추가할_수_있다() {
            ReservationWaitingRequest request = new ReservationWaitingRequest(1L);
            Member member = memberService.findById(2L);

            Long waitingId = reservationWaitingService.saveReservationWaiting(request, member);

            assertThat(waitingId)
                    .isEqualTo(2L);
        }

        @Test
        void 같은_사용자가_같은_예약에_대해선_예약_대기를_두_번_이상_추가_시_예외가_발생한다() {
            ReservationWaitingRequest request = new ReservationWaitingRequest(1L);
            Member member = memberService.findById(1L);

            assertThatThrownBy(() -> reservationWaitingService.saveReservationWaiting(request, member))
                    .isInstanceOf(DuplicatedReservationWaitingException.class);
        }
    }
}
