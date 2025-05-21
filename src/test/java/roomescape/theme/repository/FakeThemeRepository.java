package roomescape.theme.repository;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.springframework.data.domain.Pageable;
import roomescape.common.exception.NotFoundException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.theme.domain.Theme;

public class FakeThemeRepository implements ThemeRepository {

    private final List<Theme> themes = new CopyOnWriteArrayList<>();
    private final Map<Reservation, Theme> reservationThemeMap = new ConcurrentHashMap<>();
    private final AtomicLong index = new AtomicLong(1L);

    @Override
    public List<Theme> findAll() {
        return new CopyOnWriteArrayList<>(themes);
    }

    @Override
    public Optional<Theme> findById(Long id) {
        try {
            return Optional.of(themes.get((int) (id - 1)));
        } catch (IndexOutOfBoundsException e) {
            return Optional.empty();
        }
    }

    @Override
    public Theme save(Theme theme) {
        Theme saved = Theme.withId(
                index.getAndIncrement(),
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnail());

        themes.add(saved);

        return saved;
    }

    @Override
    public void deleteById(Long id) {
        Theme targetTheme = themes.stream()
                .filter(theme -> Objects.equals(theme.getId(), id))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("데이터베이스에 해당 id가 존재하지 않습니다."));

        themes.remove(targetTheme);
    }

    @Override
    public List<Theme> getRanking(ReservationDate startDate, ReservationDate endDate, Pageable pageable) {
        return findThemesWithReservationCount(startDate, endDate, pageable.getPageSize());
    }

    public void addReservation(Reservation reservation) {
        Theme theme = themes.stream()
                .filter(t -> t.getId().equals(reservation.getTheme().getId()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("테마를 찾을 수 없습니다."));

        reservationThemeMap.put(reservation, theme);
    }

    private List<Theme> findThemesWithReservationCount(ReservationDate startDate, ReservationDate endDate, int limit) {
        return reservationThemeMap.keySet().stream()
                .filter(reservation ->
                        !reservation.getDate().getValue().isBefore(startDate.getValue()) // startDate 포함
                                && !reservation.getDate().getValue().isAfter(endDate.getValue()) // endDate 포함
                )
                .collect(Collectors.groupingBy(
                        Reservation::getTheme,
                        Collectors.counting()
                ))
                .entrySet().stream()
                .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                .map(Entry::getKey)
                .limit(limit)
                .toList();

    }
}
