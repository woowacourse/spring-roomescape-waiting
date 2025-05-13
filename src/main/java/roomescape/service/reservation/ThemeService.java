package roomescape.service.reservation;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.reservation.Theme;
import roomescape.dto.reservation.AddThemeDto;
import roomescape.exception.reservation.InvalidThemeException;
import roomescape.repository.reservation.ReservationRepository;
import roomescape.repository.theme.ThemeRepository;

@Service
public class ThemeService {

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ThemeService(ThemeRepository themeRepository, ReservationRepository reservationRepository) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    public long addTheme(AddThemeDto addThemeDto) {
        Theme theme = addThemeDto.toEntity();
        return themeRepository.add(theme);
    }

    public void deleteThemeById(long id) {
        if (reservationRepository.existsByThemeId(id)) {
            throw new InvalidThemeException("예약이 존재하는 테마는 삭제할 수 없습니다.");
        }
        themeRepository.deleteById(id);
    }

    public Theme getThemeById(long id) {
        return themeRepository.findById(id)
                .orElseThrow(() -> new InvalidThemeException("존재하지 않는 테마입니다."));
    }

    public List<Theme> findAll() {
        return themeRepository.findAll();
    }
}
