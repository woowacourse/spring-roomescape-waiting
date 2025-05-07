package roomescape.fake;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.dao.ThemeDao;
import roomescape.domain.Theme;

public class FakeThemeDao implements ThemeDao {

    private final List<Theme> themes = new ArrayList<>();
    private final AtomicLong index = new AtomicLong(1);

    @Override
    public Theme add(Theme theme) {
        Theme saved = new Theme(
                index.getAndIncrement(),
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnail()
        );
        themes.add(saved);
        return saved;
    }

    @Override
    public List<Theme> findAll() {
        return new ArrayList<>(themes);
    }

    @Override
    public Theme findById(Long id) {
        return themes.stream()
            .filter(theme -> theme.getId().equals(id))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("테마를 찾을 수 없습니다."));
    }

    @Override
    public List<Theme> findMostReservedThemesInPeriodWithLimit(LocalDate startDate, LocalDate endDate, int limitCount) {
        return List.of();
    }

    @Override
    public int deleteById(Long id) {
        Optional<Theme> deleteReservationTheme = themes.stream()
                .filter(theme -> theme.getId().equals(id))
                .findFirst();
        if (deleteReservationTheme.isPresent()) {
            themes.remove(deleteReservationTheme.get());
            return 1;
        }
        return 0;
    }

    @Override
    public boolean existByName(String name) {
        return themes.stream()
                .anyMatch(theme -> theme.getName().equalsIgnoreCase(name));
    }
}
