package roomescape.domain.reservation.model.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import roomescape.domain.reservation.model.exception.ReservationException.ReservationThemeInUseException;
import roomescape.domain.reservation.model.repository.ReservationRepository;

@Component
@RequiredArgsConstructor
public class ReservationThemeValidator {

    private final ReservationRepository reservationRepository;

    public void validateNotInUse(Long reservationThemeId) {
        if (reservationRepository.existsByThemeId(reservationThemeId)) {
            throw new ReservationThemeInUseException("해당 테마를 사용중인 예약이 존재합니다.");
        }
    }
}
