package roomescape.reservation.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import roomescape.reservation.application.service.PromotionService;
import roomescape.reservation.domain.PromotionSource;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.event.schema.PromotionFailed;
import roomescape.reservation.event.schema.WaitingPromotedToReservation;

@ExtendWith(MockitoExtension.class)
class PromotionServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private PromotionService promotionService;

    private final LocalDate date = LocalDate.of(2028, 5, 6);
    private final Long themeId = 1L;
    private final Long timeId = 1L;

    @DisplayName("승격 성공 시 WaitingPromotedToReservation 이벤트를 발행한다.")
    @Test
    void publishes_WaitingPromotedToReservation_when_promoted() {
        when(reservationRepository.insertFromOldestWaiting(date, themeId, timeId)).thenReturn(true);

        promotionService.promoteFromWaiting(date, themeId, timeId, PromotionSource.DIRECT);

        verify(eventPublisher).publishEvent(new WaitingPromotedToReservation(date, themeId, timeId, PromotionSource.DIRECT));
    }

    @DisplayName("승격 중 예외 발생 시 PromotionFailed 이벤트를 retryCount+1로 발행한다.")
    @Test
    void publishes_PromotionFailed_with_incremented_retryCount_on_exception() {
        when(reservationRepository.insertFromOldestWaiting(date, themeId, timeId))
                .thenThrow(new RuntimeException("DB 오류"));

        promotionService.promoteFromWaiting(date, themeId, timeId, 1, PromotionSource.DIRECT);

        verify(eventPublisher).publishEvent(new PromotionFailed(date, themeId, timeId, 2, PromotionSource.DIRECT));
    }
}
