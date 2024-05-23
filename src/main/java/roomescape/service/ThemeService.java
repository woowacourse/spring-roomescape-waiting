package roomescape.service;

import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.Theme;
import roomescape.repository.ThemeRepository;
import roomescape.service.dto.request.ThemeCreateRequest;
import roomescape.service.dto.response.ListResponse;
import roomescape.service.dto.response.ThemeResponse;

@Service
@Transactional
public class ThemeService {

    private static final int DEFAULT_BEST_THEME_COUNT = 10;

    private final ThemeRepository themeRepository;

    public ThemeService(ThemeRepository themeRepository) {
        this.themeRepository = themeRepository;
    }

    public ListResponse<ThemeResponse> findAll() {
        List<ThemeResponse> responses = themeRepository.findAll()
                .stream()
                .map(ThemeResponse::from)
                .toList();

        return new ListResponse<>(responses);
    }

    public ListResponse<ThemeResponse> findByRanking() {
        LocalDate dateTo = LocalDate.now().minusDays(1);
        LocalDate dateFrom = dateTo.minusDays(7);

        List<ThemeResponse> responses = themeRepository.findMostReservedThemeInPeriodByCount(dateFrom, dateTo,
                        DEFAULT_BEST_THEME_COUNT)
                .stream()
                .map(ThemeResponse::from)
                .toList();

        return new ListResponse<>(responses);
    }

    public ThemeResponse save(ThemeCreateRequest themeCreateRequest) {
        Theme theme = themeCreateRequest.toRoomTheme();
        Theme savedTheme = themeRepository.save(theme);
        return ThemeResponse.from(savedTheme);
    }

    public void deleteById(Long id) {
        themeRepository.deleteById(id);
    }
}
