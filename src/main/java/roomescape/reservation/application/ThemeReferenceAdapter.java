package roomescape.reservation.application;

import org.springframework.stereotype.Component;
import roomescape.global.exception.ThemeErrorCode;
import roomescape.global.exception.customException.BusinessException;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.theme.application.ThemeReference;

@Component
public class ThemeReferenceAdapter implements ThemeReference {

    private final ReservationRepository reservationRepository;

    public ThemeReferenceAdapter(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Override
    public void validateThemeNotReferenced(Long themeId) {
        if (reservationRepository.existsByThemeId(themeId)) {
            throw new BusinessException(ThemeErrorCode.THEME_IN_USE);
        }
    }
}
