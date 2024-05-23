package roomescape.application;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import roomescape.domain.reservationdetail.Theme;
import roomescape.domain.reservationdetail.ThemeRepository;
import roomescape.exception.theme.ReservationReferencedThemeException;
import roomescape.application.dto.request.theme.ThemeRequest;
import roomescape.application.dto.response.theme.ThemeResponse;
import roomescape.application.policy.RankingPolicy;

@Service
@RequiredArgsConstructor
public class ThemeService {
    private final ThemeRepository themeRepository;

    public ThemeResponse saveTheme(ThemeRequest request) {
        Theme theme = request.toTheme();
        Theme savedTheme = themeRepository.save(theme);
        return ThemeResponse.from(savedTheme);
    }

    public List<ThemeResponse> findAllTheme() {
        List<Theme> themes = themeRepository.findAll();
        return themes.stream()
                .map(ThemeResponse::from)
                .toList();
    }

    public List<ThemeResponse> findAllPopularThemes(RankingPolicy rankingPolicy) {
        LocalDate startDate = rankingPolicy.getStartDateAsString();
        LocalDate endDate = rankingPolicy.getEndDateAsString();
        int limit = rankingPolicy.exposureSize();

        List<Theme> themes = themeRepository.findThemesByPeriodWithLimit(
                startDate.toString(), endDate.toString(), limit);

        return themes.stream()
                .map(ThemeResponse::from)
                .toList();
    }

    public void deleteTheme(Long id) {
        try {
            themeRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new ReservationReferencedThemeException();
        }
    }
}
