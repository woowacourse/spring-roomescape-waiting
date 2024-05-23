package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    public ThemeService(ThemeRepository themeRepository,
        ReservationRepository reservationRepository) {
        this.themeRepository = themeRepository;
    }

    public Theme save(String name, String description, String thumbnail) {
        Theme theme = new Theme(name, description, thumbnail);
        List<Theme> themes = themeRepository.findAll();
        theme.validateDuplication(themes);

        return themeRepository.save(theme);
    }

    @Transactional
    public void delete(Long id) {
        Theme theme = themeRepository.findById(id).orElseThrow(() ->
            new RoomescapeException("존재하지 않는 테마 id 입니다."));
        theme.validateHavingReservation();

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
