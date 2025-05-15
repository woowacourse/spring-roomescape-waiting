package roomescape.repository.fake;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

public class FakeThemeRepository implements ThemeRepository {

    private final Map<Long, Theme> data = new HashMap<>();
    private final ReservationRepository reservationRepository;
    private Long autoIncrementId = 1L;

    public FakeThemeRepository(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Override
    public Theme save(Theme theme) {
        Theme themeToSave = Theme.generateWithPrimaryKey(theme, autoIncrementId);
        data.put(autoIncrementId, themeToSave);
        autoIncrementId++;
        return themeToSave;
    }

    @Override
    public List<Theme> findAll() {
        return List.copyOf(data.values());
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) {
            return;
        }
        data.remove(id);
    }

    @Override
    public Optional<Theme> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(data.get(id));
    }

    @Override
    public List<Theme> findTopByReservationCountDesc(LocalDate fromDate, LocalDate toDate, long listNum) {
        if (listNum <= 0) {
            return List.of();
        }

        Comparator<Theme> topThemeComparator = (theme1, theme2) ->
            Integer.compare(
                countByThemeId(theme2.getId()),
                countByThemeId(theme1.getId())
            );

        List<Theme> themesBetweenDates = reservationRepository.findAll().stream()
            .filter(reservation -> reservation.getDate().isAfter(fromDate) &&
                reservation.getDate().isBefore(toDate))
            .map(Reservation::getTheme)
            .toList();

        return themesBetweenDates.stream()
            .sorted(topThemeComparator)
            .limit(listNum)
            .toList();
    }

    private int countByThemeId(Long themeId) {
        return (int) reservationRepository.findAll().stream()
            .filter(reservation -> reservation.getTheme().getId().equals(themeId))
            .count();
    }
}
