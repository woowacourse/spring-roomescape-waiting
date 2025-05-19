package roomescape.application.reservation.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.repository.ReservationRepository;
import roomescape.domain.reservation.repository.ThemeRepository;
import roomescape.infrastructure.error.exception.ThemeException;

@Service
@Transactional
public class DeleteThemeService {

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public DeleteThemeService(ThemeRepository themeRepository, ReservationRepository reservationRepository) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    public void removeById(Long themeId) {
        if (reservationRepository.existsByThemeId(themeId)) {
            throw new ThemeException("해당 테마로 예약된 예약이 존재합니다.");
        }
        themeRepository.deleteById(themeId);
    }
}
