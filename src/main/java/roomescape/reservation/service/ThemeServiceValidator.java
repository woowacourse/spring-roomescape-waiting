package roomescape.reservation.service;

import java.util.NoSuchElementException;
import org.springframework.stereotype.Component;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ThemeRepository;

@Component
public class ThemeServiceValidator {

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ThemeServiceValidator(ThemeRepository themeRepository, ReservationRepository reservationRepository) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    public void validateExistTheme(Long id) {
        if (!themeRepository.existsById(id)) {
            throw new NoSuchElementException("식별자 " + id + "에 해당하는 테마가 존재하지 않습니다. 삭제가 불가능합니다.");
        }
    }

    public void validateThemeUsage(Long id) {
        if (reservationRepository.existsBySlot_ThemeId(id)) {
            throw new IllegalStateException("식별자 " + id + "인 테마를 사용 중인 예약이 존재합니다. 삭제가 불가능합니다.");
        }
    }
}
