package roomescape.reservation.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.global.constant.GlobalConstant;
import roomescape.reservation.dao.ThemeDao;
import roomescape.reservation.model.Theme;
import roomescape.reservation.dto.request.ThemeCreateRequest;
import roomescape.reservation.exception.DuplicateThemeException;
import roomescape.global.exception.InvalidInputException;

@Service
public class ThemeService {

    public static final int TOP_RANK_PERIOD_DAYS = 7;
    public static final int TOP_RANK_THRESHOLD = 10;

    private final ThemeDao themeDao;

    public ThemeService(ThemeDao themeDao) {
        this.themeDao = themeDao;
    }

    public List<Theme> findAllThemes() {
        return themeDao.findAll();
    }

    public List<Theme> findMostReservedThemes() {
        LocalDate today = LocalDate.now(ZoneId.of(GlobalConstant.TIME_ZONE));
        return themeDao.findMostReservedThemesInPeriodWithLimit(today.minusDays(TOP_RANK_PERIOD_DAYS), today, TOP_RANK_THRESHOLD);
    }

    public Theme createTheme(ThemeCreateRequest request) {
        validateDuplicateName(request);
        Theme theme = new Theme(null, request.name(), request.description(), request.thumbnail());
        return themeDao.add(theme);
    }

    private void validateDuplicateName(ThemeCreateRequest themeCreateRequest) {
        if (themeDao.existByName(themeCreateRequest.name())) {
            throw new DuplicateThemeException();
        }
    }

    public void deleteThemeById(Long id) {
        if (themeDao.deleteById(id) == 0) {
            throw new InvalidInputException("존재하지 않는 테마 id이다.");
        }
    }
}
