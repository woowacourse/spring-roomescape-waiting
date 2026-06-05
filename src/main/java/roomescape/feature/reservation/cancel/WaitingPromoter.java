package roomescape.feature.reservation.cancel;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import roomescape.feature.reservation.domain.Reservation;
import roomescape.feature.reservation.domain.ReservationStatus;
import roomescape.feature.reservation.domain.SlotKey;
import roomescape.feature.reservation.repository.ReservationRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class WaitingPromoter {

    private static final int MAX_PROMOTION_ATTEMPTS = 5;
    private static final long PROMOTION_BACKOFF_MILLIS = 50L;
    private static final double PROMOTION_BACKOFF_MULTIPLIER = 2.0;

    private final ReservationRepository reservationRepository;

    @Retryable(
            retryFor = DataAccessException.class,
            maxAttempts = MAX_PROMOTION_ATTEMPTS,
            backoff = @Backoff(delay = PROMOTION_BACKOFF_MILLIS, multiplier = PROMOTION_BACKOFF_MULTIPLIER)
    )
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void promoteFastestWaiting(SlotKey slotKey) {
        if (reservationRepository.existsActiveReservation(slotKey)) {
            return;
        }

        Optional<Reservation> candidate = reservationRepository.findLowestIdWaitingReservation(slotKey);
        if (candidate.isEmpty()) {
            return;
        }

        int changedRowCount = reservationRepository.changeStatus(
                candidate.get().getId(), ReservationStatus.WAITING, ReservationStatus.ACTIVE);

        // 후보 대기가 그 사이 사라졌다면 다음 순번으로 재시도한다.
        if (changedRowCount <= 0) {
            promoteFastestWaiting(slotKey);
        }
    }

    @Recover
    public void recoverPromotion(DataAccessException exception, SlotKey slotKey) {
        log.error(
                "대기 예약 자동 승격에 재시도 후에도 실패했습니다. date={}, timeId={}, themeId={}",
                slotKey.date(), slotKey.timeId(), slotKey.themeId(), exception
        );
    }
}
