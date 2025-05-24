package roomescape.reservation.model.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import roomescape.reservation.model.entity.ReservationTheme;
import roomescape.reservation.model.repository.ReservationThemeRepository;

@Component
@RequiredArgsConstructor
public class ReservationThemeOperation {

    private final ReservationThemeRepository reservationThemeRepository;
    private final ReservationThemeValidator reservationThemeValidator;

    public void removeTheme(ReservationTheme reservationTheme) {
        reservationThemeValidator.validateNotUsedInActive(reservationTheme.getId());
        reservationThemeRepository.remove(reservationTheme);
    }
}
