package roomescape.application.query;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Theme;
import roomescape.domain.ThemeRepository;
import roomescape.domain.exception.NotFoundException;

@Service
@Transactional(readOnly = true)
public class ThemeQueryService {

    private final ThemeRepository themeRepository;

    public ThemeQueryService(
            ThemeRepository themeRepository
    ) {
        this.themeRepository = themeRepository;
    }

    public Theme getById(Long id) {
        return themeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("존재하지않는 테마입니다. Id: " + id));
    }

    public List<Theme> findAll() {
        return themeRepository.findAll();
    }

    public List<Theme> findPopular(LocalDate now, Integer days, Integer limit) {
        LocalDate start = now.minusDays(days);
        LocalDate end = now.minusDays(1);
        return themeRepository.findPopularThemes(start, end, limit);
    }
}
