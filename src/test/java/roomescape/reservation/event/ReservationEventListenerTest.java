package roomescape.reservation.event;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.reservation.application.service.PromotionService;
import roomescape.reservation.domain.PromotionSource;
import roomescape.reservation.event.schema.PromotionFailed;
import roomescape.reservation.event.schema.ReservationCancelRequested;

@ExtendWith(MockitoExtension.class)
class ReservationEventListenerTest {

    private static final int MAX_RETRY_COUNT = 3;

    @Mock
    private PromotionService promotionService;

    @InjectMocks
    private ReservationEventListener listener;

    private final LocalDate date = LocalDate.of(2028, 5, 6);
    private final Long themeId = 1L;
    private final Long timeId = 1L;

    @DisplayName("ReservationCancelRequested 이벤트 수신 시 promoteFromWaiting을 호출한다.")
    @Test
    void calls_promoteFromWaiting_on_cancel_requested() {
        ReservationCancelRequested event = new ReservationCancelRequested(1L, date, themeId, timeId);

        listener.handleReservationCancelRequested(event);

        verify(promotionService).promoteFromWaiting(date, themeId, timeId, PromotionSource.CANCELLATION);
    }

    @DisplayName("retryCount가 정해진 횟수 미만이면 promoteFromWaiting을 재호출한다.")
    @Test
    void retries_promoteFromWaiting_when_retryCount_less_than_threshold() {
        int retryCount = MAX_RETRY_COUNT - 1;
        PromotionFailed event = new PromotionFailed(date, themeId, timeId, retryCount, PromotionSource.CANCELLATION);

        listener.handlePromotionFailed(event);

        verify(promotionService).promoteFromWaiting(date, themeId, timeId, retryCount, PromotionSource.CANCELLATION);
    }

    @DisplayName("retryCount가 정해진 횟수 이상이면 promoteFromWaiting을 호출하지 않는다.")
    @Test
    void does_not_retry_when_retryCount_is_over_threshold() {
        PromotionFailed event = new PromotionFailed(date, themeId, timeId, MAX_RETRY_COUNT, PromotionSource.CANCELLATION);

        listener.handlePromotionFailed(event);

        verify(promotionService, never()).promoteFromWaiting(date, themeId, timeId, MAX_RETRY_COUNT, PromotionSource.CANCELLATION);
    }
}
