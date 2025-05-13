package roomescape.theme.business.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.global.exception.impl.BadRequestException;
import roomescape.reservation.business.repository.ReservationDao;
import roomescape.theme.business.domain.Theme;
import roomescape.theme.business.repository.ThemeDao;
import roomescape.theme.presentation.request.ThemeRequest;
import roomescape.theme.presentation.response.ThemeResponse;

@Service
public class ThemeService {

    private final ReservationDao reservationDao;
    private final ThemeDao themeDao;

    public ThemeService(final ReservationDao reservationDao, final ThemeDao themeDao) {
        this.reservationDao = reservationDao;
        this.themeDao = themeDao;
    }

    public List<ThemeResponse> findAll() {
        final List<Theme> themes = themeDao.findAll();
        return themes.stream()
                .map(ThemeResponse::of)
                .toList();
    }

    public ThemeResponse add(final ThemeRequest requestDto) {
        if (themeDao.existsThemeByName(requestDto.name())) {
            throw new BadRequestException("동일한 이름의 테마가 이미 존재합니다.");
        }
        final Theme theme = new Theme(requestDto.name(), requestDto.description(), requestDto.thumbnail());
        final Theme savedTheme = themeDao.save(theme);
        return ThemeResponse.of(savedTheme);
    }

    public void deleteById(final Long id) {
        if (reservationDao.existsReservationByTheme_Id(id)) {
            throw new BadRequestException("이 테마의 예약이 존재합니다.");
        }
        themeDao.deleteById(id);
    }

    public List<ThemeResponse> sortByRank() {
        final List<Theme> themes = themeDao.sortByRank();
        return themes.stream()
                .map(ThemeResponse::of)
                .toList();
    }
}
