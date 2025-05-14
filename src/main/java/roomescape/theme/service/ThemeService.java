package roomescape.theme.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.common.util.DateTime;
import roomescape.reservation.domain.ReservationPeriod;
import roomescape.reservation.infrastructure.JpaReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.dto.request.ThemeRequest;
import roomescape.theme.dto.response.PopularThemeResponse;
import roomescape.theme.dto.response.ThemeResponse;
import roomescape.theme.infrastructure.JpaThemeRepository;

@Service
public class ThemeService {

    private static final int POPULAR_THEME_COUNT = 10;
    private static final int START_OFFSET_DAYS = 8;
    private static final int END_OFFSET_DAYS = 1;

    private final DateTime dateTime;
    private final JpaThemeRepository themeRepository;
    private final JpaReservationRepository reservationRepository;

    public ThemeService(final DateTime dateTime,
                        final JpaThemeRepository themeRepository,
                        final JpaReservationRepository reservationRepository) {
        this.dateTime = dateTime;
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    public ThemeResponse createTheme(final ThemeRequest request) {
        Theme theme = Theme.createWithoutId(request.name(), request.description(), request.thumbnail());
        Theme save = themeRepository.save(theme);

        return ThemeResponse.from(save);
    }

    public void deleteThemeById(final Long id) {
        if (reservationRepository.existsByTheme_Id(id)) {
            throw new IllegalArgumentException("예약한 기록이 존재하여 삭제할 수 없습니다.");
        }
        themeRepository.deleteById(id);
    }

    public List<ThemeResponse> getThemes() {
        return themeRepository.findAll().stream()
                .map(ThemeResponse::from)
                .toList();
    }

    public List<PopularThemeResponse> getPopularThemes() {
        ReservationPeriod period = new ReservationPeriod(dateTime.nowDate(), START_OFFSET_DAYS, END_OFFSET_DAYS);

        return themeRepository.findPopularThemes(period, POPULAR_THEME_COUNT).stream()
                .map(theme -> new PopularThemeResponse(theme.getName(), theme.getThumbnail(), theme.getDescription()))
                .toList();
    }
}
