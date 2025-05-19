package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.dto.request.ThemeRequest;
import roomescape.dto.response.ThemeResponse;
import roomescape.entity.Theme;
import roomescape.exception.custom.DuplicatedException;
import roomescape.repository.jpa.JpaThemeRepository;

@Service
@Transactional
public class ThemeService {

    public static final int TOP_RANK_PERIOD_DAYS = 7;
    public static final int TOP_MAX_SIZE = 10;

    private final JpaThemeRepository themeRepository;

    public ThemeService(JpaThemeRepository themeRepository) {
        this.themeRepository = themeRepository;
    }

    @Transactional(readOnly = true)
    public List<ThemeResponse> findAllThemes() {
        return themeRepository.findAll().stream()
            .map(ThemeResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<ThemeResponse> findTopReservedThemes() {
        LocalDate today = LocalDate.now();

        return themeRepository.findTopRankByDateBetween(today.minusDays(TOP_RANK_PERIOD_DAYS), today).stream()
            .limit(TOP_MAX_SIZE)
            .map(ThemeResponse::from)
            .toList();
    }

    public ThemeResponse addTheme(ThemeRequest request) {
        validateDuplicateTheme(request);

        return ThemeResponse.from(
            themeRepository.save(new Theme(request.name(), request.description(), request.thumbnail())));
    }

    private void validateDuplicateTheme(ThemeRequest request) {
        if (themeRepository.existsByName(request.name())) {
            throw new DuplicatedException("theme");
        }
    }

    public void removeTheme(Long id) {
        themeRepository.deleteById(id);
    }
}
