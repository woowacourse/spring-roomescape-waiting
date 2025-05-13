package roomescape.infrastructure.fake;

import static java.util.Comparator.comparingLong;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.domain.Reservation;
import roomescape.domain.Theme;
import roomescape.domain.repository.ReservationRepository;
import roomescape.domain.repository.ThemeRepository;

public class ThemeFakeRepository implements ThemeRepository {

    private final Map<Long, Theme> themes = new ConcurrentHashMap<>();
    private final AtomicLong index = new AtomicLong(1L);
    private ReservationRepository reservationRepository;

    public ThemeFakeRepository() {
    }

    public ThemeFakeRepository(final ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Override
    public Optional<Theme> findById(final long id) {
        return Optional.ofNullable(themes.get(id));
    }

    @Override
    public long save(final Theme theme) {
        var id = index.getAndIncrement();
        var created = new Theme(id, theme.name(), theme.description(), theme.thumbnail());
        themes.put(id, created);
        return id;
    }

    @Override
    public boolean removeById(final long id) {
        Theme removed = themes.remove(id);
        return removed != null;
    }

    @Override
    public List<Theme> findAll() {
        return List.copyOf(themes.values());
    }

    @Override
    public List<Theme> findRankingByPeriod(final LocalDate startDate, final LocalDate endDate, final int limit) {
        var reservations = reservationRepository.findAll();
        var themeCounts = reservations.stream()
            .filter(r -> isBetween(r.date(), startDate, endDate))
            .collect(groupingBy(Reservation::theme, counting()));

        return themeCounts.keySet()
            .stream()
            .sorted(comparingLong(themeCounts::get).reversed())
            .limit(limit)
            .toList();
    }

    private boolean isBetween(final LocalDate date, final LocalDate startDate, final LocalDate endDate) {
        return (date.isAfter(startDate) || date.isEqual(startDate))
               && (date.isEqual(endDate) || date.isBefore(endDate));
    }
}
