package roomescape.application.reservation;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.reservation.dto.request.ThemeRequest;
import roomescape.application.reservation.dto.response.ThemeResponse;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.ThemeRepository;

@Service
public class ThemeService {
    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;
    private final Clock clock;

    public ThemeService(ThemeRepository themeRepository,
                        ReservationRepository reservationRepository,
                        Clock clock) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
        this.clock = clock;
    }

    public ThemeResponse create(ThemeRequest request) {
        Theme savedTheme = themeRepository.save(request.toTheme());
        return ThemeResponse.from(savedTheme);
    }

    public List<ThemeResponse> findAll() {
        return themeRepository.findAll()
                .stream()
                .map(ThemeResponse::from)
                .toList();
    }

    @Transactional
    public void deleteById(long id) {
        if (reservationRepository.existsByThemeId(id)) {
            throw new IllegalArgumentException("연관된 예약이 존재하여 삭제할 수 없습니다.");
        }
        themeRepository.deleteById(id);
    }

    public List<ThemeResponse> findPopularThemes() {
        WeeklyPopularThemeFilter policy = new WeeklyPopularThemeFilter(LocalDate.now(clock));
        return themeRepository.findPopularThemesByFilter(policy)
                .stream()
                .map(ThemeResponse::from)
                .toList();
    }
}
