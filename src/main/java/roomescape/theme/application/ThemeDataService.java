package roomescape.theme.application;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import roomescape.reservationslot.exception.ReservationSlotNotFoundException;
import roomescape.theme.domain.Theme;
import roomescape.theme.infrastructure.ThemeRepository;
import roomescape.theme.presentation.dto.request.ThemeCreateWebRequest;

@Service
public class ThemeDataService {

    private static final int DAYS_TO_SUBTRACT = 1;

    private final ThemeRepository themeRepository;

    public ThemeDataService(final ThemeRepository themeRepository) {
        this.themeRepository = themeRepository;
    }

    public Theme create(final ThemeCreateWebRequest request) {
        return themeRepository.save(request.toTheme());
    }

    public List<Theme> findAll() {
        return themeRepository.findAll();
    }

    public Theme getById(final Long id) {
        return themeRepository.findById(id)
                .orElseThrow(() -> new ReservationSlotNotFoundException("요청한 id와 일치하는 테마 정보가 없습니다."));
    }

    public Page<Theme> findPopularThemes(final Clock clock, final int days, final int limit) {
        LocalDate endDate = LocalDate.now(clock).minusDays(DAYS_TO_SUBTRACT);
        LocalDate startDate = endDate.minusDays(days);
        return themeRepository.findPopularThemes(startDate, endDate, PageRequest.of(0, limit));
    }

    public Theme save(final Theme theme) {
        return themeRepository.save(theme);
    }

    public void deleteById(final Long id) {
        themeRepository.deleteById(id);
    }
}
