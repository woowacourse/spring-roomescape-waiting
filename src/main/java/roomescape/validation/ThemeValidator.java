package roomescape.validation;

import org.springframework.stereotype.Component;
import roomescape.domain.Theme;
import roomescape.repository.ReservationRepository;

@Component
public class ThemeValidator {

    private final ReservationRepository reservationRepository;

    public ThemeValidator(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public void validateExistReservation(Theme theme) {
        if (reservationRepository.existsByTheme(theme)) {
            throw new IllegalArgumentException("예약이 등록된 테마는 제거할 수 없습니다");
        }
    }
}
