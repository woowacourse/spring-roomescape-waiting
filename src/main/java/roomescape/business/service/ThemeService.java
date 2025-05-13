package roomescape.business.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import roomescape.business.domain.Reservation;
import roomescape.business.domain.Theme;
import roomescape.exception.DuplicateException;
import roomescape.exception.NotFoundException;
import roomescape.persistence.dao.ReservationDao;
import roomescape.persistence.dao.ThemeDao;
import roomescape.presentation.dto.ThemeRequest;
import roomescape.presentation.dto.ThemeResponse;
import roomescape.util.CurrentUtil;

@Service
public class ThemeService {

    private final ThemeDao themeDao;
    private final ReservationDao reservationDao;
    private final CurrentUtil currentUtil;

    public ThemeService(final ThemeDao themeDao, final ReservationDao reservationDao, final CurrentUtil currentUtil) {
        this.themeDao = themeDao;
        this.reservationDao = reservationDao;
        this.currentUtil = currentUtil;
    }

    public ThemeResponse insert(final ThemeRequest themeRequest) {
        validateNameIsNotDuplicate(themeRequest.name());
        final Theme theme = themeRequest.toDomain();
        final Long id = themeDao.insert(theme)
                .getId();
        return new ThemeResponse(id, theme.getName(), theme.getDescription(), theme.getThumbnail());
    }

    private void validateNameIsNotDuplicate(final String name) {
        if (themeDao.existsByName(name)) {
            throw new DuplicateException("추가 하려는 테마 이름이 이미 존재합니다.");
        }
    }

    public List<ThemeResponse> findAll() {
        return themeDao.findAll()
                .stream()
                .map(ThemeResponse::from)
                .toList();
    }

    public ThemeResponse findById(final Long id) {
        final Theme theme = themeDao.findById(id)
                .orElseThrow(() -> new NotFoundException("해당하는 테마를 찾을 수 없습니다. 테마 id: %d".formatted(id)));
        return ThemeResponse.from(theme);
    }

    public void deleteById(final Long id) {
        if (!themeDao.deleteById(id)) {
            throw new NotFoundException("해당하는 테마를 찾을 수 없습니다. 테마 id: %d".formatted(id));
        }
    }

    public List<ThemeResponse> findPopularThemes() {
        final LocalDate now = currentUtil.getCurrentDate();
        final String endDate = now.toString();
        final String startDate = now.minusDays(7)
                .toString();
        return findPopularThemesBetween(startDate, endDate);
    }

    private List<ThemeResponse> findPopularThemesBetween(final String startDate, final String endDate) {
        final List<Theme> themes = themeDao.findAll();
        final List<Reservation> reservations = reservationDao.findByDateBetween(startDate, endDate);
        final Map<Long, Long> themeReservationCount = calculateThemeReservationCount(reservations);
        final List<Theme> sortedThemes = sortedThemesByReservationCount(themes, themeReservationCount);
        return sortedThemes.stream()
                .map(ThemeResponse::from)
                .toList();
    }

    private Map<Long, Long> calculateThemeReservationCount(final List<Reservation> reservations) {
        return reservations.stream()
                .collect(Collectors.groupingBy(
                        reservation -> reservation.getTheme()
                                .getId(),
                        Collectors.counting()
                ));
    }

    private List<Theme> sortedThemesByReservationCount(final List<Theme> themes,
                                                       final Map<Long, Long> themeReservationCount) {
        return themes.stream()
                .sorted((theme1, theme2) -> {
                    final Long theme1ReservationCount = themeReservationCount.getOrDefault(theme1.getId(), 0L);
                    final Long theme2ReservationCount = themeReservationCount.getOrDefault(theme2.getId(), 0L);
                    return theme2ReservationCount.compareTo(theme1ReservationCount);
                })
                .toList();
    }
}
