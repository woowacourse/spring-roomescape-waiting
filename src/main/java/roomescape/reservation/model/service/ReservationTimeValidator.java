package roomescape.reservation.model.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import roomescape.reservation.model.exception.ReservationException.ReservationTimeInUseException;
import roomescape.reservation.model.repository.ReservationRepository;

@Component
@RequiredArgsConstructor
public class ReservationTimeValidator {

    private final ReservationRepository reservationRepository;

    public void validateNotActive(Long reservationTimeId) {
        if (reservationRepository.existsActiveByTimeId(reservationTimeId)) {
            throw new ReservationTimeInUseException("해당 예약 시간을 사용중인 예약이 존재합니다.");
        }
    }
}
