package roomescape.repository.fake;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.repository.ReservationRepository;

public class FakeReservationRepository implements ReservationRepository {

    private final Map<Long, Reservation> store = new HashMap<>();
    private long nextId = 1L;

    @Override
    public List<Reservation> findAllByStoreIds(List<Long> storeIds, int limit, int offset) {
        return store.values().stream()
                .filter(r -> storeIds.contains(r.getStore().getId()))
                .sorted(Comparator.comparing(Reservation::getId))
                .skip(offset)
                .limit(limit)
                .toList();
    }

    @Override
    public List<Reservation> findAllByStoreIdsAndName(List<Long> storeIds, String name, int limit, int offset) {
        return store.values().stream()
                .filter(r -> storeIds.contains(r.getStore().getId()))
                .filter(r -> r.getUser().getName().equals(name))
                .sorted(Comparator.comparing(Reservation::getId))
                .skip(offset)
                .limit(limit)
                .toList();
    }

    @Override
    public List<Reservation> findAllByUserId(Long userId) {
        return store.values().stream()
                .filter(r -> r.getUser().getId().equals(userId))
                .sorted(Comparator.comparing(Reservation::getId))
                .toList();
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Long save(Reservation reservation) {
        Long id = nextId++;
        store.put(id, reservation.withId(id));
        return id;
    }

    @Override
    public int deleteById(Long id) {
        return store.remove(id) == null ? 0 : 1;
    }

    @Override
    public int update(Reservation reservation) {
        if (!store.containsKey(reservation.getId())) {
            return 0;
        }
        store.put(reservation.getId(), reservation);
        return 1;
    }

    @Override
    public List<Long> findTimeIdsByThemeIdAndDate(Long themeId, LocalDate date) {
        return store.values().stream()
                .filter(r -> r.getTheme().getId().equals(themeId))
                .filter(r -> r.getDate().equals(date))
                .map(r -> r.getTime().getId())
                .toList();
    }

    @Override
    public boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId) {
        return store.values().stream()
                .anyMatch(r -> r.getDate().equals(date)
                        && r.getTime().getId().equals(timeId)
                        && r.getTheme().getId().equals(themeId));
    }

    @Override
    public boolean existsByDateAndTimeAndThemeAndStoreAndUser(LocalDate date, Long timeId, Long themeId, Long storeId,
                                                              Long userId) {
        return store.values().stream()
                .anyMatch(r -> r.getDate().equals(date)
                        && r.getTime().getId().equals(timeId)
                        && r.getTheme().getId().equals(themeId)
                        && r.getStore().getId().equals(storeId)
                        && r.getUser().getId().equals(userId));
    }

    @Override
    public boolean existsByReservationTimeId(Long timeId) {
        return store.values().stream()
                .anyMatch(r -> r.getTime().getId().equals(timeId));
    }

    @Override
    public boolean existsByDateAndTimeAndThemeAndStoreAndStatus(LocalDate date, Long timeId, Long themeId, Long storeId,
                                                                ReservationStatus status) {
        return store.values().stream()
                .anyMatch(r -> r.getDate().equals(date)
                        && r.getTime().getId().equals(timeId)
                        && r.getTheme().getId().equals(themeId)
                        && r.getStore().getId().equals(storeId)
                        && r.getStatus().equals(status));
    }

    Collection<Reservation> all() {
        return Collections.unmodifiableCollection(store.values());
    }
}
