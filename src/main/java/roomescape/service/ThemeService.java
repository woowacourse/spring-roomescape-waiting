package roomescape.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import roomescape.domain.repository.ThemeRepository;
import roomescape.dto.AvailableTimeResponse;
import roomescape.dto.ThemeResponse;

@Service
public class ThemeService {
    private final ThemeRepository themeRepository;

    public ThemeService(ThemeRepository themeRepository) {
        this.themeRepository = themeRepository;
    }

    public List<ThemeResponse> getPopularThemes(LocalDate today, int size) {
        LocalDate endDate = today.minusDays(1);
        LocalDate startDate = today.minusDays(7);

        return themeRepository.findPopularThemes(size, startDate, endDate).stream()
                .map(ThemeResponse::from)
                .toList();
    }

    public List<ThemeResponse> getAllThemes() {
        return themeRepository.findAll().stream()
                .map(ThemeResponse::from)
                .toList();
    }

    public List<AvailableTimeResponse> getAvailableTimeResponses(Long themId, String date) {
        return themeRepository.findAvailableTimeById(themId, date);
    }
}
