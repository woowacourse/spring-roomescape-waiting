package roomescape.theme.service;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.repository.ReservationRepository;
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

    public ThemeResponse create(ThemeCreateRequest request) {
        Theme theme = new Theme(request.name(), request.description(), request.thumbnail());
        Theme savedTheme = themeRepository.save(theme);
        return ThemeResponse.from(savedTheme);
    }

    @Transactional
    public void deleteById(Long id) {
        if (reservationRepository.existsByThemeId(id)) {
            throw new IllegalArgumentException("[ERROR] 해당 테마에 예약이 존재하여 삭제할 수 없습니다. 입력값:" + id);
        }
        Theme theme = getTheme(id);
        themeRepository.deleteById(theme.getId());
    }

    @Transactional(readOnly = true)
    public List<ThemeResponse> getAll() {
        List<Theme> themes = themeRepository.findAll();
        return ThemeResponse.from(themes);
    }

    @Transactional(readOnly = true)
    public Theme getTheme(Long id) {
        return themeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("[ERROR] 해당 테마가 존재하지 않습니다."));
    }

    @Transactional(readOnly = true)
    public List<ThemeResponse> getPopularThemes() {
        LocalDate endDate = LocalDate.now().minusDays(1L);
        LocalDate startDate = LocalDate.now().minusDays(7L);
        List<Theme> topThemesByReservationCount = reservationRepository.findTopThemesByReservationCount(startDate,
                endDate, PageRequest.of(0, 10));

        return ThemeResponse.from(topThemesByReservationCount);
    }

}
