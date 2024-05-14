package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.domain.Theme;
import roomescape.domain.dto.ThemeRequest;
import roomescape.domain.dto.ThemeResponse;
import roomescape.domain.dto.ThemeResponses;
import roomescape.exception.DeleteNotAllowException;
import roomescape.repository.ReservationDao;
import roomescape.repository.ThemeDao;

import java.time.LocalDate;
import java.util.List;

@Service
public class ThemeService {
    private final ThemeDao themeDao;
    private final ReservationDao reservationDao;

    public ThemeService(final ThemeDao themeDao, final ReservationDao reservationDao) {
        this.themeDao = themeDao;
        this.reservationDao = reservationDao;
    }

    public ThemeResponses findAll() {
        final List<ThemeResponse> themeResponses = themeDao.findAll()
                .stream()
                .map(ThemeResponse::from)
                .toList();
        return new ThemeResponses(themeResponses);
    }

    public ThemeResponse create(final ThemeRequest themeRequest) {
        Long id = themeDao.create(themeRequest);
        Theme theme = themeRequest.toEntity(id);
        return ThemeResponse.from(theme);
    }

    public void delete(final Long id) {
        validateExistReservation(id);
        themeDao.delete(id);
    }

    public ThemeResponses getPopularThemeList(final LocalDate startDate, final LocalDate endDate, final Long count) {
        final List<Theme> themes = themeDao.findPopularThemeByDate(startDate, endDate, count);
        final List<ThemeResponse> themeResponses = themes.stream().map(ThemeResponse::from).toList();
        return new ThemeResponses(themeResponses);
    }

    private void validateExistReservation(final Long id) {
        if (reservationDao.isExistsThemeId(id)) {
            throw new DeleteNotAllowException("예약이 등록된 테마는 제거할 수 없습니다.");
        }
    }
}
