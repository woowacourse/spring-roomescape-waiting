package roomescape.reservation.application.service;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.domain.PromotionSource;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.event.schema.PromotionFailed;
import roomescape.reservation.event.schema.WaitingPromotedToReservation;

@Service
@RequiredArgsConstructor
public class PromotionService {

    private final ReservationRepository reservationRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void promoteFromWaiting(LocalDate date, Long themeId, Long timeId, PromotionSource source) {
        promoteFromWaiting(date, themeId, timeId, 0, source);
    }

    @Transactional
    public void promoteFromWaiting(LocalDate date, Long themeId, Long timeId, int retryCount, PromotionSource source) {
        try {
            boolean promoted = reservationRepository.insertFromOldestWaiting(date, themeId, timeId);
            if (promoted) {
                eventPublisher.publishEvent(new WaitingPromotedToReservation(date, themeId, timeId, source));
            }
        } catch (Exception e) {
            eventPublisher.publishEvent(new PromotionFailed(date, themeId, timeId, retryCount + 1, source));
        }
    }
}
