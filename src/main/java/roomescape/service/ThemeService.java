package roomescape.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import roomescape.dto.AvailableTimeResponse;
import roomescape.dto.ThemeResponse;
import roomescape.repository.ThemeDao;

@Service
public class ThemeService {

    private final ThemeDao themeDao;

    public ThemeService(ThemeDao themeDao) {
        this.themeDao = themeDao;
    }

    public List<ThemeResponse> getPopularThemes(LocalDate today, int size) {
        LocalDate endDate = today.minusDays(1);
        LocalDate startDate = today.minusDays(7);
        return themeDao.findPopularThemes(size, startDate, endDate).stream()
                .map(ThemeResponse::from)
                .toList();
    }

    public List<ThemeResponse> getAllThemes() {
        return themeDao.findAll().stream()
                .map(ThemeResponse::from)
                .toList();
    }

    public List<AvailableTimeResponse> getAvailableTimeResponses(long themeId, String date) {
        return themeDao.findAvailableTimeById(themeId, date);
    }
}
