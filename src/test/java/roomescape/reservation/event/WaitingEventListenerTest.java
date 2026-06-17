package roomescape.reservation.event;

import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.reservation.application.service.PromotionService;
import roomescape.reservation.application.service.WaitingCommandService;
import roomescape.reservation.domain.PromotionSource;
import roomescape.reservation.event.schema.WaitingPromotedToReservation;
import roomescape.reservation.event.schema.WaitingSaved;

@ExtendWith(MockitoExtension.class)
class WaitingEventListenerTest {

    @Mock
    private PromotionService promotionService;

    @Mock
    private WaitingCommandService waitingCommandService;

    @InjectMocks
    private WaitingEventListener listener;

    private final LocalDate date = LocalDate.of(2028, 5, 6);
    private final Long themeId = 1L;
    private final Long timeId = 1L;

    @DisplayName("WaitingSaved 이벤트 수신 시 promoteFromWaiting을 호출한다.")
    @Test
    void calls_promoteFromWaiting_on_waiting_saved() {
        WaitingSaved event = new WaitingSaved(date, themeId, timeId);

        listener.handleWaitingSaved(event);

        verify(promotionService).promoteFromWaiting(date, themeId, timeId, PromotionSource.DIRECT);
    }

    @DisplayName("WaitingPromotedToReservation 이벤트 수신 시 deleteOldestBySlot을 호출한다.")
    @Test
    void calls_deleteOldestBySlot_on_waiting_promoted() {
        WaitingPromotedToReservation event = new WaitingPromotedToReservation(date, themeId, timeId, PromotionSource.DIRECT);

        listener.handleWaitingPromoted(event);

        verify(waitingCommandService).deleteOldestBySlot(date, themeId, timeId);
    }
}
