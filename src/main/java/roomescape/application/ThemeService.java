package roomescape.application;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.dto.ThemeCreateServiceRequest;
import roomescape.application.dto.ThemeServiceResponse;
import roomescape.domain.entity.Theme;
import roomescape.domain.repository.ThemeRepository;
import roomescape.exception.NotFoundException;

@Service
@Transactional(readOnly = true)
public class ThemeService {

    public static final int RANKING_LIMIT_COUNT = 10;
    private final ThemeRepository themeRepository;

    public ThemeService(ThemeRepository themeRepository) {
        this.themeRepository = themeRepository;
    }

    @Transactional
    public ThemeServiceResponse registerTheme(@Valid ThemeCreateServiceRequest createDto) {
        Theme themeWithoutId = Theme.withoutId(createDto.name(), createDto.description(), createDto.thumbnail());
        Theme savedTheme = themeRepository.save(themeWithoutId);
        return ThemeServiceResponse.from(savedTheme);
    }

    public List<ThemeServiceResponse> getAllThemes() {
        List<Theme> themes = themeRepository.findAll();
        return ThemeServiceResponse.from(themes);
    }

    public ThemeServiceResponse getThemeById(Long id) {
        Theme theme = themeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("id에 해당하는 테마가 없습니다."));
        return ThemeServiceResponse.from(theme);
    }

    public List<ThemeServiceResponse> getThemeRanking() {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusWeeks(1);
        LocalDate endDate = today.minusDays(1);
        List<Theme> themeRanking = themeRepository.findThemeRanking(startDate, endDate, RANKING_LIMIT_COUNT);
        return ThemeServiceResponse.from(themeRanking);
    }

    @Transactional
    public void deleteTheme(Long id) {
        try {
            themeRepository.deleteById(id);
            themeRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("예약이 존재하는 테마는 삭제할 수 없습니다.");
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("삭제하려는 id가 존재하지 않습니다. id: " + id);
        }
    }
}
