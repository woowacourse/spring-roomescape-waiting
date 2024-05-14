package roomescape.theme.service;

import org.springframework.stereotype.Service;
import roomescape.exception.BadRequestException;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.dto.ThemeCreateRequest;
import roomescape.theme.dto.ThemeResponse;
import roomescape.theme.repository.ThemeRepository;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ThemeService {

    private static final long POPULAR_THEME_PERIOD = 7L;
    private static final long POPULAR_THEME_COUNT = 10L;

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ThemeService(ThemeRepository themeRepository, ReservationRepository reservationRepository) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    public ThemeResponse createTheme(ThemeCreateRequest request) {
        Theme theme = request.toTheme();
        validateDuplicated(theme);
        Theme savedTheme = themeRepository.save(theme);
        return ThemeResponse.from(savedTheme);
    }

    private void validateDuplicated(Theme theme) {
        boolean isExists = themeRepository.existsByName(theme.getName());
        if (isExists) {
            throw new BadRequestException("중복된 테마 이름입니다.");
        }
    }

    public List<ThemeResponse> readThemes() {
        return themeRepository.findAll().stream()
                .map(ThemeResponse::from)
                .toList();
    }

    public List<ThemeResponse> readPopularThemes() {
        LocalDate end = LocalDate.now().minusDays(1L);
        LocalDate start = end.minusDays(POPULAR_THEME_PERIOD);

        Map<Long, Long> reservationCountByTheme = collectReservationByTheme(start, end);

        return reservationCountByTheme.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(POPULAR_THEME_COUNT)
                .map(e -> readTheme(e.getKey()))
                .toList();
    }

    private Map<Long, Long> collectReservationByTheme(LocalDate start, LocalDate end) {
        return reservationRepository.findByDateBetween(start, end).stream()
                .collect(Collectors.groupingBy(reservation -> reservation.getTheme().getId(), Collectors.counting()));
    }

    public ThemeResponse readTheme(Long id) {
        Theme theme = themeRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("존재하지 않는 테마입니다."));
        return ThemeResponse.from(theme);
    }

    public void deleteTheme(Long id) {
        validateReservationExists(id);
        int deletedCount = themeRepository.deleteById(id);
        if (deletedCount == 0) {
            throw new BadRequestException("존재하지 않는 테마입니다.");
        }
    }

    private void validateReservationExists(Long id) {
        if (reservationRepository.existsByThemeId(id)) {
            throw new BadRequestException("해당 테마에 예약이 존재합니다.");
        }
    }
}
