package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.theme.Name;
import roomescape.domain.theme.Theme;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeRepository;
import roomescape.system.exception.RoomescapeException;

@Service
public class ThemeService {

    private static final int POPULAR_START_DATE = 8;
    private static final int POPULAR_END_DATE = 1;
    private static final int POPULAR_THEME_COUNT = 10;

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ThemeService(ThemeRepository themeRepository,
        ReservationRepository reservationRepository) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    public Theme save(String name, String description, String thumbnail) {
        Theme theme = new Theme(name, description, thumbnail);
        validateDuplication(name);
        return themeRepository.save(theme);
    }

    private void validateDuplication(String name) {
        if (themeRepository.existsByName(new Name(name))) {
            throw new RoomescapeException("같은 이름의 테마가 이미 존재합니다.");
        }
    }

    public void delete(Long id) {
        if (reservationRepository.existsByThemeId(id)) {
            throw new RoomescapeException("해당 테마를 사용하는 예약이 존재하여 삭제할 수 없습니다.");
        }
        themeRepository.deleteById(id);
    }

    public List<Theme> findAll() {
        return themeRepository.findAll();
    }

    public List<Theme> findPopular() {
        LocalDate start = LocalDate.now().minusDays(POPULAR_START_DATE);
        LocalDate end = LocalDate.now().minusDays(POPULAR_END_DATE);
        return themeRepository.findPopular(start, end, POPULAR_THEME_COUNT);
    }
}
