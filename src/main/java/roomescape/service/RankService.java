package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.Theme;
import roomescape.dto.response.ThemeResponse;
import roomescape.repository.RankDao;

@Service
public class RankService {
    private final RankDao rankDao;

    public RankService(final RankDao rankDao) {
        this.rankDao = rankDao;
    }

    public List<ThemeResponse> getPopularThemeList() {
        LocalDate currentDate = LocalDate.now();
        final List<Theme> topTenByDate = rankDao.findTopTenByDate(currentDate.minusWeeks(1),
                currentDate.minusDays(1));
        return topTenByDate.stream().map(ThemeResponse::from).toList();
    }
}
