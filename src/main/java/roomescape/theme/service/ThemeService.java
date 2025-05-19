package roomescape.theme.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.exception.ReservationTimeInUseException;
import roomescape.theme.domain.Theme;
import roomescape.theme.dto.request.ThemeCreateRequest;
import roomescape.theme.dto.response.ThemeResponse;
import roomescape.theme.repository.ThemeRepository;

@Service
public class ThemeService {

    private static final int DEFAULT_POPULAR_SIZE = 10;
    private static final int POPULAR_START_DAYS = 7;
    private static final int POPULAR_END_DAYS = 1;

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ThemeService(final ThemeRepository themeRepository, final ReservationRepository reservationRepository) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<ThemeResponse> getThemes() {
        return themeRepository.findAll().stream()
                .map(ThemeResponse::from)
                .toList();
    }

    public void delete(Long id) {
        if (reservationRepository.existsByThemeId(id)) {
            throw new ReservationTimeInUseException("해당 테마에 대한 예약이 존재하여 삭제할 수 없습니다.");
        }
        themeRepository.deleteById(id);
    }

    public ThemeResponse create(final ThemeCreateRequest request) {
        Theme theme = themeRepository.save(request.toTheme());
        return ThemeResponse.from(theme);
    }

    public List<ThemeResponse> getPopularThemes() {
        final LocalDate date = LocalDate.now();
        Pageable pageable = PageRequest.of(0, DEFAULT_POPULAR_SIZE);
        return themeRepository.findTop10PopularThemesWithinLastWeek(date.minusDays(POPULAR_START_DAYS),
                        date.minusDays(POPULAR_END_DAYS), pageable)
                .stream()
                .map(ThemeResponse::from)
                .toList();
    }
}
