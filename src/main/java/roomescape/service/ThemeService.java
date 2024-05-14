package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.Theme;
import roomescape.dto.request.ThemeRequest;
import roomescape.dto.response.ThemeResponse;
import roomescape.repository.ReservationDao;
import roomescape.repository.ThemeDao;

@Service
public class ThemeService {
    private final ThemeDao themeDao;
    private final ReservationDao reservationDao;

    public ThemeService(ThemeDao themeDao, ReservationDao reservationDao) {
        this.themeDao = themeDao;
        this.reservationDao = reservationDao;
    }

    public List<ThemeResponse> findAll() {
        return themeDao.findAll()
                .stream()
                .map(ThemeResponse::from)
                .toList();
    }

    public ThemeResponse findById(Long id) {
        Theme theme = themeDao.findById(id);
        return ThemeResponse.from(theme);
    }

    public ThemeResponse create(ThemeRequest themeRequest) {
        Long id = themeDao.create(themeRequest);
        Theme theme = themeRequest.toEntity(id);
        return ThemeResponse.from(theme);
    }

    public void delete(Long id) {
        validateExistReservation(id);
        themeDao.delete(id);
    }

    private void validateExistReservation(Long id) {
        if (reservationDao.isExistsThemeId(id)) {
            throw new IllegalArgumentException("[ERROR] 예약이 등록된 테마는 제거할 수 없습니다");
        }
    }
}
