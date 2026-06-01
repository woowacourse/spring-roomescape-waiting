package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.dao.ReservationDao;
import roomescape.dao.ThemeDao;
import roomescape.domain.AvailableTime;
import roomescape.domain.Theme;
import roomescape.service.exception.ReservationConflictException;

@Service
public class ThemeService {
    private final ThemeDao themeDao;
    private final ReservationDao reservationDao;

    public ThemeService(ThemeDao themeDao, ReservationDao reservationDao) {
        this.themeDao = themeDao;
        this.reservationDao = reservationDao;
    }

    @Transactional(readOnly = true)
    public List<Theme> getPopularThemes(int size, LocalDate startDate, LocalDate endDate) {
        return themeDao.findPopularThemes(size, startDate, endDate);
    }

    @Transactional(readOnly = true)
    public List<Theme> getAllThemes() {
        return themeDao.findAll();
    }

    @Transactional(readOnly = true)
    public List<AvailableTime> getAvailableTimes(long themeId, LocalDate date) {
        return themeDao.findAvailableTimeById(themeId, date);
    }

    @Transactional
    public long save(String name, String description, String thumbnailUrl) {
        return themeDao.save(name, description, thumbnailUrl);
    }

    @Transactional
    public void delete(long id) {
        if (reservationDao.existsByThemeId(id)) {
            throw new ReservationConflictException("예약에 사용 중인 테마는 삭제할 수 없습니다.");
        }
        themeDao.delete(id);
    }
}
