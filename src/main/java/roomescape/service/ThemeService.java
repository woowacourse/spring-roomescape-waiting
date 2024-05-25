package roomescape.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import roomescape.domain.Theme;
import roomescape.domain.policy.RankingPolicy;
import roomescape.domain.policy.WeeklyRankingPolicy;
import roomescape.domain.repository.ThemeRepository;
import roomescape.exception.theme.NotFoundThemeException;
import roomescape.exception.theme.ReservationReferencedThemeException;
import roomescape.service.dto.request.theme.ThemeRequest;
import roomescape.service.dto.response.theme.ThemeResponse;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ThemeService {
    private final ThemeRepository themeRepository;

    @Transactional
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

    public List<ThemeResponse> findAllPopularTheme() {
        RankingPolicy rankingPolicy = new WeeklyRankingPolicy();
        LocalDate startDate = rankingPolicy.getStartDateAsString();
        LocalDate endDate = rankingPolicy.getEndDateAsString();
        int limit = rankingPolicy.exposureSize();

        PageRequest pageRequest = PageRequest.of(0, limit);
        List<Theme> themes = themeRepository.findThemeByPeriodWithLimit(startDate, endDate, pageRequest);

        return themes.stream()
                .map(ThemeResponse::from)
                .toList();
    }

    @Transactional
    public void deleteTheme(Long themeId) {
        Theme theme = themeRepository.findById(themeId)
                .orElseThrow(NotFoundThemeException::new);

        try {
            themeRepository.delete(theme);
        } catch (DataIntegrityViolationException e) {
            throw new ReservationReferencedThemeException();
        }
    }
}
