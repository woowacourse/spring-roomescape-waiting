package roomescape.theme.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.ConflictException;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

import java.time.LocalDate;
import java.util.List;

@Service
public class ThemeService {
    private final ThemeRepository themeRepository;

    public ThemeService(ThemeRepository themeRepository) {
        this.themeRepository = themeRepository;
    }

    @Transactional
    public Theme create(String name, String description, String thumbnail) {
        if (themeRepository.existsByName(name)) {
            throw new ConflictException("이미 등록된 테마 이름입니다. 다른 이름을 입력해주세요.");
        }
        Theme theme = Theme.create(name, description, thumbnail);

        return themeRepository.save(theme);
    }

    @Transactional(readOnly = true)
    public List<Theme> list() {
        return themeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Theme> findPopularThemes(int days, LocalDate now, int size) {
        LocalDate startDate = now.minusDays(days);
        LocalDate endDate = now.minusDays(1);

        return themeRepository.findTopThemesByReservationCount(startDate, endDate, size);
    }

    @Transactional
    public void activate(Long id) {
        themeRepository.updateActive(id, true);
    }

    @Transactional
    public void deactivate(Long id) {
        themeRepository.updateActive(id, false);
    }
}
