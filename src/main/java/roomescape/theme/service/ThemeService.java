package roomescape.theme.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.exception.BadRequestException;
import roomescape.theme.domain.Theme;
import roomescape.theme.dto.request.ThemeCreateRequest;
import roomescape.theme.dto.response.ThemeResponse;
import roomescape.theme.repository.ThemeRepository;

@Service
@AllArgsConstructor
public class ThemeService {

    private final ThemeRepository themeRepository;

    public ThemeResponse create(ThemeCreateRequest request) {
        Theme theme = new Theme(
            null,
            request.name(),
            request.description(),
            request.thumbnail()
        );
        return ThemeResponse.fromTheme(themeRepository.save(theme));
    }

    public List<ThemeResponse> findAll() {
        return themeRepository.findAll().stream()
            .map(ThemeResponse::fromTheme)
            .toList();
    }

    public void deleteThemeById(Long id) {
        themeRepository.deleteById(id);
    }

    public List<ThemeResponse> findLimitedThemesByPopularDesc() {
        LocalDate fromDate = LocalDate.now().minusDays(7);
        LocalDate toDate = LocalDate.now().minusDays(1);
        int size = 10;

        return themeRepository.findTopReservedThemesInPeriod(fromDate, toDate, size).stream()
            .map(ThemeResponse::fromTheme)
            .toList();
    }

    public Optional<Theme> findById(Long id) {
        return themeRepository.findById(id);
    }

    public Theme findByIdOrThrow(Long id) {
        return themeRepository.findById(id)
            .orElseThrow(() -> new BadRequestException("존재하지 않는 테마입니다."));
    }
}
