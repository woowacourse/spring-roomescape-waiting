package roomescape.application;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import roomescape.application.dto.ThemeCreateDto;
import roomescape.application.dto.ThemeDto;
import roomescape.domain.Theme;
import roomescape.domain.repository.ThemeRepository;
import roomescape.exception.NotFoundException;

@Service
public class ThemeService {

    public static final int RANKING_LIMIT_COUNT = 10;
    private final ThemeRepository themeRepository;

    public ThemeService(ThemeRepository themeRepository) {
        this.themeRepository = themeRepository;
    }

    public List<ThemeDto> getAllThemes() {
        List<Theme> themes = themeRepository.findAll();
        return ThemeDto.from(themes);
    }

    public ThemeDto registerTheme(@Valid ThemeCreateDto createDto) {
        Theme themeWithoutId = Theme.withoutId(createDto.name(), createDto.description(), createDto.thumbnail());
        Long id = themeRepository.save(themeWithoutId);
        Theme theme = Theme.assignId(id, themeWithoutId);
        return ThemeDto.from(theme);
    }

    public void deleteTheme(Long id) {
        boolean deleted;
        try {
            deleted = themeRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("예약이 존재하는 테마는 삭제할 수 없습니다.");
        }
        if (!deleted) {
            throw new NotFoundException("삭제하려는 id가 존재하지 않습니다. id: " + id);
        }
    }

    public ThemeDto getThemeById(Long id) {
        Theme theme = themeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("id에 해당하는 테마가 없습니다."));
        return ThemeDto.from(theme);
    }

    public List<ThemeDto> getThemeRanking() {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusWeeks(1);
        LocalDate endDate = today.minusDays(1);
        List<Theme> themeRanking = themeRepository.findThemeRanking(RANKING_LIMIT_COUNT, startDate, endDate);
        return ThemeDto.from(themeRanking);
    }
}
