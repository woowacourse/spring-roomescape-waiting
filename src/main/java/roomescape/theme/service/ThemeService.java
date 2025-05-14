package roomescape.theme.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.common.exception.DuplicateException;
import roomescape.common.exception.ForeignKeyException;
import roomescape.common.exception.InvalidIdException;
import roomescape.common.exception.message.IdExceptionMessage;
import roomescape.common.exception.message.ThemeExceptionMessage;
import roomescape.reservation.dao.ReservationDao;
import roomescape.theme.dao.ThemeDao;
import roomescape.theme.domain.Theme;
import roomescape.theme.dto.RankedThemeResponse;
import roomescape.theme.dto.ThemeRequest;
import roomescape.theme.dto.ThemeResponse;

@Service
public class ThemeService {

    private final ReservationDao reservationDao;
    private final ThemeDao themeDao;

    public ThemeService(ThemeDao themeDao, ReservationDao reservationDao) {
        this.themeDao = themeDao;
        this.reservationDao = reservationDao;
    }

    public List<ThemeResponse> findAll() {
        return themeDao.findAll().stream()
                .map(theme -> new ThemeResponse(
                        theme.getId(),
                        theme.getName(),
                        theme.getDescription(),
                        theme.getThumbnail()))
                .toList();
    }

    public List<RankedThemeResponse> findRankedByPeriod() {
        List<Theme> topRankedThemes = themeDao.findRankedByPeriod(
                LocalDate.now().minusDays(7),
                LocalDate.now().minusDays(1)
        );
        return topRankedThemes.stream()
                .map(theme -> new RankedThemeResponse(
                        theme.getName(),
                        theme.getDescription(),
                        theme.getThumbnail())
                )
                .toList();
    }

    public ThemeResponse add(final ThemeRequest themeRequest) {
        validateDuplicate(themeRequest);
        Theme newTheme = new Theme(themeRequest.name(), themeRequest.description(), themeRequest.thumbnail());
        Theme savedTheme = themeDao.add(newTheme);

        return new ThemeResponse(
                savedTheme.getId(),
                savedTheme.getName(),
                savedTheme.getDescription(),
                savedTheme.getThumbnail()
        );
    }

    private void validateDuplicate(final ThemeRequest themeRequest) {
        boolean isDuplicate = themeDao.existsByName(themeRequest.name());

        if (isDuplicate) {
            throw new DuplicateException(ThemeExceptionMessage.DUPLICATE_THEME.getMessage());
        }
    }

    public void deleteById(final Long id) {
        validateThemeId(id);
        validateUnoccupiedThemeId(id);
        themeDao.deleteById(id);
    }

    private void validateThemeId(final Long id) {
        themeDao.findById(id)
                .orElseThrow(() -> new InvalidIdException(IdExceptionMessage.INVALID_THEME_ID.getMessage()));
    }

    private void validateUnoccupiedThemeId(final Long id) {
        boolean isOccupiedThemeId = themeDao.existsByReservationThemeId(id);

        if (isOccupiedThemeId) {
            throw new ForeignKeyException(ThemeExceptionMessage.RESERVED_THEME.getMessage());
        }
    }
}
