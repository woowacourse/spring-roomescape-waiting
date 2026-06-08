package roomescape.application;

import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import roomescape.exception.ConflictException;
import roomescape.exception.ErrorCode;
import roomescape.reservation.Reservation;
import roomescape.reservation.service.ReservationService;
import roomescape.reservationwaiting.ReservationWaiting;
import roomescape.reservationwaiting.service.ReservationWaitingService;

@Service
public class ReservationApplicationService {
    private static final Logger log = LoggerFactory.getLogger(ReservationApplicationService.class);
    private final ReservationService reservationService;
    private final ReservationWaitingService reservationWaitingService;
    private final WaitingPromotionService waitingPromotionService;

    public ReservationApplicationService(
            final ReservationService reservationService,
            final ReservationWaitingService reservationWaitingService,
            final WaitingPromotionService waitingPromotionService
    ) {
        this.reservationService = reservationService;
        this. reservationWaitingService = reservationWaitingService;
        this.waitingPromotionService = waitingPromotionService;
    }

    public ReservationWaiting saveWaiting(String name, LocalDate date, Long themeId, Long timeId) {

        Reservation reservation = reservationService.findByDateAndThemeIdAndTimeId(date, themeId, timeId);
        validateReservationOwner(name, reservation);

        return reservationWaitingService.save(name, date, themeId, timeId);
    }

    public void cancelReservation(Long id, String name) {
        Reservation reservation = reservationService.deleteByIdAndName(id, name);

        try {
            waitingPromotionService.promoteFirstWaiting(reservation);
        } catch (Exception e) {
            log.warn("대기 승격 실패", e.getMessage());
        }
    }

    private void validateReservationOwner(String name, Reservation reservation) {

        if (name.equals(reservation.getName())) {
            throw new ConflictException(
                    ErrorCode.RESERVATION_WAITING_DUPLICATED,
                    "이미 예약한 사람은 같은 예약에 대기할 수 없습니다."
            );
        }
    }

}
