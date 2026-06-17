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
    private boolean failDeleteOnce;
    private boolean failUpdateWaitingToReservedOnce;

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
                .sorted(Comparator.comparing(Reservation::getStatus)
                        .thenComparing(Reservation::getDate)
                        .thenComparing(r -> r.getTime().getStartAt())
                        .thenComparing(Reservation::getId))
                .toList();
    }

    @Override
    public Map<Reservation, Integer> findWaitingReservationsWithOrderByUserId(Long userId) {
        return store.values().stream()
                .filter(r -> r.getStatus().equals(ReservationStatus.WAITING))
                .sorted(Comparator.comparing(Reservation::getDate)
                        .thenComparing(r -> r.getTime().getStartAt())
                        .thenComparing(Reservation::getId))
                .filter(r -> r.getUser().getId().equals(userId))
                .collect(java.util.stream.Collectors.toMap(
                        reservation -> reservation,
                        this::waitingOrder,
                        (left, right) -> left,
                        java.util.LinkedHashMap::new
                ));
    }

    @Override
    public Optional<Reservation> findFirstWaitingReservationByDateAndTimeAndThemeAndStoreForUpdate(LocalDate date,
                                                                                                   Long timeId,
                                                                                                   Long themeId,
                                                                                                   Long storeId) {
        return store.values().stream()
                .filter(r -> r.getStatus().equals(ReservationStatus.WAITING))
                .filter(r -> r.getDate().equals(date))
                .filter(r -> r.getTime().getId().equals(timeId))
                .filter(r -> r.getTheme().getId().equals(themeId))
                .filter(r -> r.getStore().getId().equals(storeId))
                .min(Comparator.comparing(Reservation::getId));
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
        if (failDeleteOnce) {
            failDeleteOnce = false;
            return 0;
        }
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
    public int updateStatus(Long id, ReservationStatus status) {
        Reservation reservation = store.get(id);
        if (reservation == null) {
            return 0;
        }
        store.put(id, reservation.withStatus(status));
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
    public boolean existsReservedByDateAndTimeAndThemeAndStore(LocalDate date, Long timeId, Long themeId,
                                                               Long storeId) {
        return store.values().stream()
                .anyMatch(r -> r.getDate().equals(date)
                        && r.getTime().getId().equals(timeId)
                        && r.getTheme().getId().equals(themeId)
                        && r.getStore().getId().equals(storeId)
                        && r.getStatus().equals(ReservationStatus.RESERVED));
    }

    @Override
    public boolean existsReservedOrPaymentPendingByDateAndTimeAndThemeAndStore(LocalDate date, Long timeId,
                                                                               Long themeId, Long storeId) {
        return store.values().stream()
                .anyMatch(r -> r.getDate().equals(date)
                        && r.getTime().getId().equals(timeId)
                        && r.getTheme().getId().equals(themeId)
                        && r.getStore().getId().equals(storeId)
                        && (r.getStatus().equals(ReservationStatus.RESERVED)
                        || r.getStatus().equals(ReservationStatus.PAYMENT_PENDING)));
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

    Collection<Reservation> all() {
        return Collections.unmodifiableCollection(store.values());
    }

    private int waitingOrder(Reservation target) {
        return (int) store.values().stream()
                .filter(r -> r.getStatus().equals(ReservationStatus.WAITING))
                .filter(r -> r.getDate().equals(target.getDate()))
                .filter(r -> r.getTime().getId().equals(target.getTime().getId()))
                .filter(r -> r.getTheme().getId().equals(target.getTheme().getId()))
                .filter(r -> r.getStore().getId().equals(target.getStore().getId()))
                .filter(r -> r.getId() <= target.getId())
                .count();
    }
}
