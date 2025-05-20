package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import roomescape.domain.Reservation;
import roomescape.domain.Theme;
import roomescape.dto.request.ThemeRequest;
import roomescape.dto.response.ThemeResponse;
import roomescape.exception.ExistedReservationException;
import roomescape.exception.ExistedThemeException;
import roomescape.infrastructure.ReservationRepository;
import roomescape.infrastructure.ThemeRepository;

@Service
public class ThemeService {

    private static final int TOP_THEMES_COUNT = 10;
    private static final int DAYS_BEFORE_START = 7;
    private static final int DAYS_BEFORE_END = 1;

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ThemeService(ThemeRepository themeRepository, ReservationRepository reservationRepository) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    public ThemeResponse createTheme(ThemeRequest themeRequest) {
        Optional<Theme> optionalTheme = themeRepository.findByName(themeRequest.name());
        if (optionalTheme.isPresent()) {
            throw new ExistedThemeException();
        }

        Theme theme = Theme.createWithoutId(themeRequest.name(), themeRequest.description(), themeRequest.thumbnail());
        Theme themeWithId = themeRepository.save(theme);
        return new ThemeResponse(themeWithId.getId(), themeWithId.getName(), themeWithId.getDescription(),
                themeWithId.getThumbnail());
    }

    public List<ThemeResponse> findAllThemes() {
        List<Theme> themes = themeRepository.findAll();
        return themes.stream()
                .map(theme ->
                        new ThemeResponse(
                                theme.getId(),
                                theme.getName(),
                                theme.getDescription(),
                                theme.getThumbnail()
                        )
                )
                .toList();
    }

    public void deleteThemeById(long id) {
        List<Reservation> reservations = reservationRepository.findByThemeId(id);
        if (reservations.size() > 0) {
            throw new ExistedReservationException();
        }
        themeRepository.deleteById(id);
    }

    public List<ThemeResponse> getTopThemes() {
        LocalDate startDate = LocalDate.now().minusDays(DAYS_BEFORE_START);
        LocalDate endDate = LocalDate.now().minusDays(DAYS_BEFORE_END);
        List<Reservation> reservations = reservationRepository.findByDateBetween(startDate, endDate);

        Map<Theme, Long> themeCount = countTheme(reservations);
        List<Theme> themes = themeCount
                .entrySet().stream()
                .sorted(Entry.<Theme, Long>comparingByValue().reversed())
                .limit(TOP_THEMES_COUNT)
                .map(Entry::getKey)
                .toList();

        return themes.stream().map(theme -> new ThemeResponse(theme.getId(), theme.getName(), theme.getDescription(),
                theme.getThumbnail())).toList();
    }

    private Map<Theme, Long> countTheme(List<Reservation> reservations) {
        return reservations.stream()
                .collect(Collectors.groupingBy(Reservation::getTheme, Collectors.counting()));
    }
}
