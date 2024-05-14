package roomescape.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import roomescape.domain.Theme;
import roomescape.domain.policy.RankingPolicy;
import roomescape.domain.repository.ThemeRepository;
import roomescape.exception.theme.NotFoundThemeException;
import roomescape.exception.theme.ReservationReferencedThemeException;
import roomescape.web.dto.request.theme.ThemeRequest;
import roomescape.web.dto.response.theme.ThemeResponse;

@Service
@RequiredArgsConstructor
public class ThemeService {
    private final ThemeRepository themeRepository;

    public List<ThemeResponse> findAllTheme() {
        List<Theme> themes = themeRepository.findAll();
        return themes.stream()
                .map(ThemeResponse::from)
                .toList();
    }

    public List<ThemeResponse> findAllPopularTheme(RankingPolicy rankingPolicy) {
        LocalDate startDate = rankingPolicy.getStartDateAsString();
        LocalDate endDate = rankingPolicy.getEndDateAsString();
        int limit = rankingPolicy.exposureSize();

        List<Theme> themes = themeRepository.findThemesByPeriodWithLimit(startDate.toString(), endDate.toString(),
                limit);
        return themes.stream()
                .map(ThemeResponse::from)
                .toList();
    }

    public ThemeResponse saveTheme(ThemeRequest request) {
        Theme theme = request.toTheme();
        Theme savedTheme = themeRepository.save(theme);
        return ThemeResponse.from(savedTheme);
    }

    public void deleteTheme(Long id) {
        Theme theme = findThemeById(id);
        try {
            themeRepository.delete(theme);
        } catch (DataIntegrityViolationException e) {
            throw new ReservationReferencedThemeException();
        }
    }

    private Theme findThemeById(Long id) {
        return themeRepository.findById(id)
                .orElseThrow(NotFoundThemeException::new);
    }
}
