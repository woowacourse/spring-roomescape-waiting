package roomescape.theme.service;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import roomescape.reservation.service.out.ReservationRepository;
import roomescape.theme.controller.request.ThemeCreateRequest;
import roomescape.theme.controller.response.ThemeResponse;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.out.ThemeRepository;

@Service
public class ThemeService {

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ThemeService(ThemeRepository themeRepository, ReservationRepository reservationRepository) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    public void deleteById(Long id) {
        if (reservationRepository.existReservationByThemeId(id)) {
            throw new IllegalArgumentException("[ERROR] 해당 테마에 예약이 존재하여 삭제할 수 없습니다.");
        }
        Theme theme = getTheme(id);
        themeRepository.deleteById(theme.getId());
    }

    public ThemeResponse create(ThemeCreateRequest request) {
        Theme created = Theme.create(request.name(), request.description(), request.thumbnail());
        Theme saved = themeRepository.save(created);
        return ThemeResponse.from(saved);
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
        int limit = 10;
        LocalDate to = LocalDate.now();
        LocalDate from = to.minusDays(7);

        return themeRepository.findAll().stream()
                .sorted((t1, t2) -> Integer.compare(
                        reservationRepository.countReservationByThemeIdAndDuration(from, to, t1.getId()),
                        reservationRepository.countReservationByThemeIdAndDuration(from, to, t2.getId())
                ))
                .limit(limit)
                .map(ThemeResponse::from)
                .toList();
    }
}
