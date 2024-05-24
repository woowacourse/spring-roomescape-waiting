package roomescape.service;

import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.Theme;
import roomescape.exception.BadRequestException;
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
        LocalDate dateFrom = LocalDate.now().minusDays(7);

        List<ThemeResponse> responses = themeRepository.findMostReservedThemeInPeriodByCount(dateFrom, dateTo,
                        DEFAULT_BEST_THEME_COUNT)
                .stream()
                .map(ThemeResponse::from)
                .toList();

        return new ListResponse<>(responses);
    }

    public ThemeResponse save(ThemeCreateRequest themeCreateRequest) {
        Theme theme = themeCreateRequest.toTheme();
        validateIsSameNameExist(theme.getName());

        Theme savedTheme = themeRepository.save(theme);
        return ThemeResponse.from(savedTheme);
    }

    private void validateIsSameNameExist(String name) {
        boolean exists = themeRepository.existsByName(name);
        if (exists) {
            throw new BadRequestException("이미 존재하는 테마 이름입니다.(" + name + ")");
        }
    }

    public void deleteById(Long id) {
        themeRepository.deleteById(id);
    }
}
