package roomescape.repository;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import roomescape.domain.Reservation;
import roomescape.domain.Theme;

public class CollectionThemeRepository implements ThemeRepository {
    private final List<Theme> themes;
    private final AtomicLong index;
    private final CollectionReservationRepository reservationRepository;

    public CollectionThemeRepository() {
        this(new ArrayList<>(), new AtomicLong(0), null);
    }

    private CollectionThemeRepository(
            List<Theme> themes,
            AtomicLong index,
            CollectionReservationRepository reservationRepository
    ) {
        this.themes = themes;
        this.index = index;
        this.reservationRepository = reservationRepository;
    }

    @Override
    public List<Theme> findAll() {
        return new ArrayList<>(themes);
    }

    @Override
    public List<Theme> findAndOrderByPopularity(LocalDate start, LocalDate end, int count) {
        if (reservationRepository == null) {
            throw new UnsupportedOperationException("ReservationRepository 를 사용해 생성하지 않아 메서드를 사용할 수 없습니다.");
        }

        Map<Long, Integer> collect = reservationRepository.findAll().stream()
                .filter(reservation -> isAfterStart(start, reservation))
                .filter(reservation -> isBeforeEnd(end, reservation))
                .map(Reservation::getTheme)
                .collect(Collectors.groupingBy(Theme::getId, Collectors.summingInt(value -> 1)));

        return collect.keySet()
                .stream()
                .sorted((o1, o2) -> Integer.compare(collect.get(o2), collect.get(o1)))
                .map(id -> findById(id).orElseThrow())
                .toList();
    }

    private boolean isAfterStart(LocalDate start, Reservation reservation) {
        LocalDate date = reservation.getDate();
        return date.isAfter(start) || date.isEqual(start);
    }

    private boolean isBeforeEnd(LocalDate end, Reservation reservation) {
        LocalDate date = reservation.getDate();
        return date.isBefore(end) || date.isEqual(end);
    }

    @Override
    public Optional<Theme> findById(long id) {
        return themes.stream()
                .filter(theme -> theme.isIdOf(id))
                .findFirst();
    }

    @Override
    public Theme save(Theme theme) {
        Theme saved = new Theme(index.incrementAndGet(), theme);
        themes.add(saved);
        return saved;
    }

    @Override
    public void delete(long id) {
        themes.removeIf(theme -> theme.isIdOf(id));
    }
}
