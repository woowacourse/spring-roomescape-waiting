package roomescape.theme.service;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.global.error.exception.ConflictException;
import roomescape.global.error.exception.NotFoundException;
import roomescape.theme.dto.request.ThemeRequest.ThemeCreateRequest;
import roomescape.theme.dto.response.ThemeResponse.ThemeCreateResponse;
import roomescape.theme.dto.response.ThemeResponse.ThemeReadResponse;
import roomescape.theme.entity.Theme;
import roomescape.theme.repository.ThemeRepository;

@Service
@RequiredArgsConstructor
public class ThemeService {

    private final ThemeRepository themeRepository;

    public ThemeCreateResponse createTheme(ThemeCreateRequest request) {
        Theme newTheme = request.toEntity();
        themeRepository.findByName(newTheme.getName())
                .ifPresent(theme -> {
                    throw new ConflictException("이미 존재하는 테마 이름입니다.");
                });
        Theme saved = themeRepository.save(newTheme);
        return ThemeCreateResponse.from(saved);
    }

    public List<ThemeReadResponse> getAllThemes() {
        return themeRepository.findAll()
                .stream()
                .map(ThemeReadResponse::from)
                .toList();
    }

    public List<ThemeReadResponse> getPopularThemes(int limit) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusWeeks(1);
        return themeRepository.findPopularDescendingUpTo(startDate, endDate, limit)
                .stream()
                .map(ThemeReadResponse::from)
                .toList();
    }

    public void deleteTheme(Long id) {
        boolean deleted = themeRepository.deleteById(id);
        if (!deleted) {
            throw new NotFoundException("존재하지 않는 테마입니다.");
        }
    }
}
