package roomescape.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.dao.ReservationDao;
import roomescape.dao.ReservationWaitingDao;
import roomescape.domain.service.WaitingPromotionResult;
import roomescape.domain.service.WaitingPromotionService;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationWaiting;
import roomescape.exception.ResourceNotFoundException;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReservationAdminCommandService {

    private final WaitingPromotionService promotionService;
    private final ReservationDao reservationDao;
    private final ReservationWaitingDao waitingDao;
    private final Clock clock;

    @Transactional
    public void delete(Long reservationId) {
        Reservation reservation = reservationDao.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("삭제하려는 예약이 존재하지 않습니다."));

        List<ReservationWaiting> waitings = waitingDao.findAllBySlot(reservation.getSlot());
        Optional<WaitingPromotionResult> promotion = promotionService.promote(waitings);

        reservationDao.delete(reservation);

        if (isPromotable(promotion)) {
            reservationDao.create(promotion.get().promotedReservation());
            waitingDao.delete(promotion.get().targetWaiting());
        }
    }

    private boolean isPromotable(Optional<WaitingPromotionResult> promotion) {
        return promotion.isPresent() && !promotion.get().promotedReservation().isPast(LocalDateTime.now(clock));
    }
}
