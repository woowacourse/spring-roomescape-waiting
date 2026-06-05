package roomescape.reservation.application;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import roomescape.global.exception.ReservationTimeErrorCode;
import roomescape.global.exception.customException.BusinessException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservationTime.application.ReservationTimeReference;
import roomescape.reservationTime.domain.ReservationTime;

@Component
@RequiredArgsConstructor
public class ReservationTimeReferenceAdapter implements ReservationTimeReference {

    private final ReservationRepository reservationRepository;

    @Override
    public void validateReservationTimeNotReferenced(Long timeId) {
        if (reservationRepository.existsByReservationTimeId(timeId)) {
            throw new BusinessException(ReservationTimeErrorCode.RESERVATION_TIME_IN_USE);
        }
    }

    @Override
    public List<ReservationTime> getBookedTimes(LocalDate date, Long themeId) {
        return reservationRepository.findByDateAndThemeId(date, themeId)
                .stream()
                .map(Reservation::getTime)
                .toList();
    }
}
