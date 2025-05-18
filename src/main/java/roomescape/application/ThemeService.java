package roomescape.application;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.dto.ThemeCreateDto;
import roomescape.application.dto.ThemeDto;
import roomescape.domain.Theme;
import roomescape.domain.repository.ThemeRepository;
import roomescape.exception.NotFoundException;

@Service
@Transactional
public class ThemeService {

    public static final int RANKING_LIMIT_COUNT = 10;
    private final ThemeRepository themeRepository;
    private final ClockProvider clockProvider;

    public ThemeService(ThemeRepository themeRepository, ClockProvider clockProvider) {
        this.themeRepository = themeRepository;
        this.clockProvider = clockProvider;
    }

    public ThemeDto registerTheme(@Valid ThemeCreateDto createDto) {
        Theme themeWithoutId = Theme.withoutId(createDto.name(), createDto.description(), createDto.thumbnail());
        Theme savedTheme = themeRepository.save(themeWithoutId);
        return ThemeDto.from(savedTheme);
    }

    @Transactional(readOnly = true)
    public ThemeDto getThemeById(Long id) {
        Theme theme = themeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("id에 해당하는 테마가 없습니다."));
        return ThemeDto.from(theme);
    }

    @Transactional(readOnly = true)
    public List<ThemeDto> getAllThemes() {
        List<Theme> themes = themeRepository.findAll();
        return ThemeDto.from(themes);
    }

    @Transactional(readOnly = true)
    public List<ThemeDto> getThemeRanking() {
        LocalDate now = clockProvider.now().toLocalDate();
        LocalDate startDate = now.minusWeeks(1);
        LocalDate endDate = now.minusDays(1);
        List<Theme> themeRanking = themeRepository.findThemeRanking(startDate, endDate, RANKING_LIMIT_COUNT);
        return ThemeDto.from(themeRanking);
    }

    public void deleteTheme(Long id) {
        try {
            themeRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("예약이 존재하는 테마는 삭제할 수 없습니다.");
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("삭제하려는 id가 존재하지 않습니다. id: " + id);
        }
    }
}
