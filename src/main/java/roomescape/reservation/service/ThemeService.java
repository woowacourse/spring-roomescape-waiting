package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import roomescape.reservation.controller.dto.request.ThemeSaveRequest;
import roomescape.reservation.controller.dto.response.ThemeDeleteResponse;
import roomescape.reservation.controller.dto.response.ThemeResponse;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.repository.ReservationDao;
import roomescape.reservation.repository.ThemeDao;

@Service
public class ThemeService {
    private final ThemeDao themeDao;
    private final ReservationDao reservationDao;

    public ThemeService(final ThemeDao themeDao, final ReservationDao reservationDao) {
        this.themeDao = themeDao;
        this.reservationDao = reservationDao;
    }

    public ThemeResponse save(final ThemeSaveRequest themeSaveRequest) {
        Theme theme = themeSaveRequest.toEntity();
        return ThemeResponse.from(themeDao.save(theme));
    }

    public List<ThemeResponse> getAll() {
        return themeDao.getAll()
                .stream()
                .map(ThemeResponse::from)
                .toList();
    }

    public List<ThemeResponse> findThemeRanking() {
        List<Long> themeRankingIds = reservationDao.findRanking(
                LocalDate.now().minusWeeks(1),
                LocalDate.now(),
                10
        );
        return themeRankingIds.stream()
                .map(id -> ThemeResponse.from(themeDao.findById(id).get()))
                .toList();
    }

    public ThemeDeleteResponse delete(final long id) {
        if (themeDao.findById(id).isEmpty()) {
            throw new NoSuchElementException("[ERROR] (id : " + id + ") 에 대한 테마가 존재하지 않습니다.");
        }
        if (!reservationDao.findByThemeId(id).isEmpty()) {
            throw new IllegalArgumentException("[ERROR] 해당 테마를 사용 중인 예약이 있어 삭제할 수 없습니다.");
        }
        return new ThemeDeleteResponse(themeDao.delete(id));
    }
}
