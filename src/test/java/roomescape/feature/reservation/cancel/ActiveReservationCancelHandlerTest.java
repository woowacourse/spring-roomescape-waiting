package roomescape.feature.reservation.cancel;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ActiveReservationCancelHandlerTest {

    private static final Long TIME_ID = 1L;
    private static final Long THEME_ID = 1L;
    private static final LocalDate DATE = LocalDate.now().plusYears(1);

    @Mock
    private WaitingPromoter waitingPromoter;

    @InjectMocks
    private ActiveReservationCancelHandler reservationCancelHandler;

    @Test
    void 이벤트를_받으면_대기_승격을_위임한다() {
        // given
        ActiveReservationCancelEvent event = new ActiveReservationCancelEvent(TIME_ID, THEME_ID, DATE);

        // when
        reservationCancelHandler.confirmFastestWaiting(event);

        // then
        verify(waitingPromoter).promoteFastestWaiting(event);
    }

    @Test
    void 승격_위임_중_예외가_발생해도_예외를_전파하지_않는다() {
        // given
        ActiveReservationCancelEvent event = new ActiveReservationCancelEvent(TIME_ID, THEME_ID, DATE);
        doThrow(new RuntimeException("승격 실패")).when(waitingPromoter).promoteFastestWaiting(event);

        // when & then
        assertThatNoException().isThrownBy(() -> reservationCancelHandler.confirmFastestWaiting(event));
    }
}
