package roomescape;

import java.time.LocalDate;
import org.springframework.stereotype.Service;
import roomescape.exception.ConflictException;
import roomescape.exception.ErrorCode;
import roomescape.reservation.Reservation;
import roomescape.reservation.service.ReservationService;
import roomescape.reservationwaiting.ReservationWaiting;
import roomescape.reservationwaiting.service.ReservationWaitingService;

@Service
public class ReservationApplicationService {
    private final ReservationService reservationService;
    private final ReservationWaitingService reservationWaitingService;

    public ReservationApplicationService(
            final ReservationService reservationService,
            final ReservationWaitingService reservationWaitingService
    ) {
        this.reservationService = reservationService;
        this.reservationWaitingService = reservationWaitingService;
    }

    public ReservationWaiting saveWaiting(String name, LocalDate date, Long themeId, Long timeId) {
        validateWaiting(name, date, themeId, timeId);

        return reservationWaitingService.save(name, date, themeId, timeId);
    }

    private void validateWaiting(String name, LocalDate date, Long themeId, Long timeId) {

        Reservation reservation = reservationService.findByDateAndThemeIdAndTimeId(date, themeId, timeId);

        if (name.equals(reservation.getName())) {
            throw new ConflictException(
                    ErrorCode.RESERVATION_WAITING_DUPLICATED,
                    "이미 예약한 사람은 같은 예약에 대기할 수 없습니다."
            );
        }
    }

}
