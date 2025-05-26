package roomescape.unit.fake;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.springframework.data.domain.Pageable;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.dto.response.ThemeResponse;
import roomescape.reservation.infrastructure.ReservationRepository;
import roomescape.reservation.infrastructure.ThemeRepository;

public class FakeThemeRepository implements ThemeRepository {

    private final List<Theme> themes;
    private final AtomicLong index;
    private final ReservationRepository reservationRepository;

    public FakeThemeRepository(ReservationRepository reservationRepository) {
        themes = new ArrayList<>();
        index = new AtomicLong(1);
        this.reservationRepository = reservationRepository;
    }

    @Override
    public Theme save(Theme theme) {
        Theme themeWithId = Theme.builder()
                .id(index.getAndIncrement())
                .name(theme.getName())
                .description(theme.getDescription())
                .thumbnail(theme.getThumbnail()).build();
        themes.add(themeWithId);
        return themeWithId;
    }

    @Override
    public List<Theme> findAll() {
        return themes;
    }

    @Override
    public void deleteById(long id) {
        themes.removeIf(theme -> theme.getId().equals(id));
    }

    @Override
    public Optional<Theme> findByName(String name) {
        return themes.stream()
                .filter(theme -> theme.getName().equals(name))
                .findFirst();
    }

    @Override
    public Optional<Theme> findById(Long id) {
        return themes.stream().filter(theme -> theme.getId().equals(id)).findFirst();
    }

    @Override
    public List<ThemeResponse> findByDateBetweenOrderByReservationCountDescNameAsc(LocalDate dateFrom, LocalDate dateTo,
                                                                                   Pageable pageable) {
        List<Reservation> reservations = reservationRepository.findAll();
        Map<Theme, Long> themeCount = reservations.stream()
                .filter(reservation -> {
                    LocalDate date = reservation.getReservationTime().getDate();
                    return !date.isBefore(dateFrom) && !date.isAfter(dateTo);
                })
                .collect(Collectors.groupingBy(Reservation::getTheme, Collectors.counting()));
        List<Theme> themes = themeCount
                .entrySet().stream()
                .sorted(Entry.<Theme, Long>comparingByValue().reversed())
                .limit(pageable.getPageSize())
                .map(Entry::getKey)
                .toList();

        return themes.stream().map(theme -> new ThemeResponse(theme.getId(), theme.getName(), theme.getDescription(),
                theme.getThumbnail())).toList();
    }
}
