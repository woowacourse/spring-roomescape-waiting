package roomescape.theme.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.service.ReservationRepository;
import roomescape.theme.controller.request.ThemeCreateRequest;
import roomescape.theme.controller.response.ThemeResponse;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

@Service
public class ThemeService {

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ThemeService(ThemeRepository themeRepository, ReservationRepository reservationRepository) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    public void deleteById(Long id) {
        if (reservationRepository.existsByThemeId(id)) {
            throw new IllegalArgumentException("[ERROR] 해당 테마에 예약이 존재하여 삭제할 수 없습니다.");
        }
        Theme theme = getTheme(id);
        themeRepository.deleteById(theme.getId());
    }

    public ThemeResponse create(ThemeCreateRequest request) {
        Theme theme = new Theme(request.name(), request.description(), request.thumbnail());
        Theme savedTheme = themeRepository.save(theme);
        return ThemeResponse.from(savedTheme);
    }

    public List<ThemeResponse> getAll() {
        List<Theme> themes = themeRepository.findAll();
        return ThemeResponse.from(themes);
    }

    public Theme getTheme(Long id) {
        return themeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("[ERROR] 해당 테마가 존재하지 않습니다."));
    }

    public List<ThemeResponse> getPopularThemes() {
        LocalDate endDate = LocalDate.now().minusDays(1L);
        LocalDate startDate = LocalDate.now().minusDays(7L);

        List<Reservation> recentReservations = reservationRepository.findAllByReservationDateBetween(startDate,
                endDate);

        List<Theme> themesSortedByPopularity = sortThemesByReservationCount(recentReservations);

        return ThemeResponse.from(themesSortedByPopularity);
    }

    private List<Theme> sortThemesByReservationCount(List<Reservation> recentReservations) {
        Map<Theme, Integer> themeCount = new HashMap<>();

        recentReservations
                .forEach(reservation -> themeCount.put(reservation.getTheme(),
                        themeCount.getOrDefault(reservation.getTheme(), 0) + 1));

        return themeCount.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))
                .map(Map.Entry::getKey)
                .toList();
    }

}
