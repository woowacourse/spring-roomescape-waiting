package roomescape.theme.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.theme.dao.ThemeDao;
import roomescape.theme.dto.response.ReservedThemeResponse;

@Service
public class ThemeRankingService {

    private final ThemeDao themeDao;

    public ThemeRankingService(ThemeDao themeDao) {
        this.themeDao = themeDao;
    }

    public List<ReservedThemeResponse> findMostReserved(long limit, LocalDate startDate, LocalDate endDate) {
        if (endDate != null) {
            return themeDao.findMostReserved(limit, startDate, endDate);
        }

        return themeDao.findMostReserved(limit, startDate, LocalDate.now().minusDays(1));
    }
}
