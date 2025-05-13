package roomescape.repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import roomescape.domain.Reservation;
import roomescape.domain.Theme;

public class FakeThemeRepository implements ThemeRepository {

    private final List<Theme> themes;
    private final AtomicLong themeId;
    private final List<Reservation> reservations = new ArrayList<>();

    public FakeThemeRepository(final List<Theme> themes) {
        this.themes = new ArrayList<>(themes);
        this.themeId = new AtomicLong(themes.size() + 1);
    }

    @Override
    public Optional<Theme> save(Theme theme) {
        long count = themes.stream()
                .filter(t -> t.getName().equals(theme.getName()) && t.getDescription().equals(theme.getDescription()) && t.getThumbnail().equals(theme.getThumbnail()))
                .count();
        if (count != 0) {
            throw new DuplicateKeyException("동일한 테마가 존재합니다.");
        }

        Theme newTheme = new Theme(themeId.getAndIncrement(), theme.getName(), theme.getDescription(), theme.getThumbnail());
        themes.add(newTheme);
        return findById(newTheme.getId());
    }

    @Override
    public List<Theme> findAll() {
        return themes;
    }

    @Override
    public Optional<Theme> findById(long id) {
        return themes.stream()
                .filter(theme -> Objects.equals(theme.getId(), id))
                .findFirst();
    }

    @Override
    public List<Theme> findMostReservedByDateRange(LocalDate start, LocalDate end) {
        Map<Theme, Long> themeCounts = reservations.stream()
                .filter(r -> r.getDate().isAfter(start.minusDays(1)) && r.getDate().isBefore(end))
                .collect(Collectors.groupingBy(Reservation::getTheme, Collectors.counting()));

        return themeCounts.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(10)
                .map(Map.Entry::getKey)
                .toList();
    }

    @Override
    public int deleteById(long id) {
        Theme deleteTheme = themes.stream()
                .filter(theme -> Objects.equals(theme.getId(), id))
                .findFirst()
                .orElse(new Theme(null, "A", "b", "https://"));

        if (deleteTheme.getId() != null) {
            if (reservations.stream()
                    .filter(reservation -> reservation.getTheme().equals(deleteTheme))
                    .count() != 0) {
                throw new DataIntegrityViolationException("연결된 예약 데이터로 인해 삭제할 수 없습니다.");
            }
            int affectedRows = (int) themes.stream()
                    .filter(theme -> Objects.equals(theme.getId(), id))
                    .count();
            themes.remove(deleteTheme);
            return affectedRows;
        }

        return 0;
    }

    public void addReservation(Reservation reservation) {
        reservations.add(reservation);
    }
}
