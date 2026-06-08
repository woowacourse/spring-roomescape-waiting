package roomescape.waiting.application;

import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WaitingPromotionSchedulerTest {

    @Test
    @DisplayName("예약 없는 대기 승격 스케줄을 실행한다")
    void promoteWaitingWithoutReservation_success() {
        // given
        WaitingService waitingService = mock(WaitingService.class);
        WaitingPromotionScheduler scheduler = new WaitingPromotionScheduler(waitingService);

        // when
        scheduler.promoteWaitingWithoutReservation();

        // then
        then(waitingService).should().promoteWaitingWithoutReservation();
    }
}
