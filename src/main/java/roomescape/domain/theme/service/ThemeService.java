package roomescape.domain.theme.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.reservation.repository.ReservationRepository;
import roomescape.domain.theme.domain.Theme;
import roomescape.domain.theme.dto.ThemeAddRequest;
import roomescape.domain.theme.repository.ThemeRepository;
import roomescape.global.exception.EscapeApplicationException;

@Service
public class ThemeService {

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ThemeService(ThemeRepository themeRepository, ReservationRepository reservationRepository) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<Theme> findAllTheme() {
        return themeRepository.findAll();
    }

    public Theme addTheme(ThemeAddRequest themeAddRequest) {
        Theme theme = themeAddRequest.toEntity();
        return themeRepository.insert(theme);
    }

    public void removeTheme(Long id) {
        if (themeRepository.findById(id).isEmpty()) {
            throw new EscapeApplicationException("해당 id를 가진 테마가 존재하지 않습니다.");
        }
        themeRepository.deleteById(id);
    }

    public List<Theme> getThemeRanking() {
        return reservationRepository.findThemeOrderByReservationCount();
    }
}
