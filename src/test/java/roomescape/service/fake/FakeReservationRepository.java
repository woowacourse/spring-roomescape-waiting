package roomescape.service.fake;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationEntry;
import roomescape.repository.ReservationRepository;
import roomescape.repository.dto.ReservationCondition;

public class FakeReservationRepository implements ReservationRepository {

    private final Map<Long, Reservation> storage = new HashMap<>();
    private final AtomicLong reservationCounter = new AtomicLong(1);
    private final AtomicLong entryCounter = new AtomicLong(1);

    @Override
    public Reservation save(Reservation reservation) {
        Long id = reservation.getId() == null ? reservationCounter.getAndIncrement() : reservation.getId();
        Reservation saved = new Reservation(
                id,
                reservation.getDate(),
                reservation.getTheme(),
                reservation.getTime(),
                copyEntriesWithId(reservation)
        );
        storage.put(saved.getId(), saved);
        return saved;
    }

    private List<ReservationEntry> copyEntriesWithId(Reservation reservation) {
        return reservation.getEntries()
                .stream()
                .map(entry -> new ReservationEntry(
                        entry.getId() == null ? entryCounter.getAndIncrement() : entry.getId(),
                        entry.getName(),
                        entry.getStatus(),
                        entry.getCreatedAt()
                ))
                .toList();
    }

    @Override
    public Optional<Reservation> findById(long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Optional<Reservation> findByDateAndThemeAndTimeForUpdate(ReservationCondition condition) {
        return storage.values()
                .stream()
                .filter(value -> value.getDate().equals(condition.date()) &&
                        value.getTheme().getId().equals(condition.themeId()) &&
                        value.getTime().getId().equals(condition.timeId()))
                .findFirst();
    }

    @Override
    public void update(Reservation reservation) {
        storage.put(reservation.getId(), reservation);
    }

    @Override
    public Optional<Reservation> findByEntryIdForUpdate(long entryId) {
        return storage.values()
                .stream()
                .filter(reservation -> reservation.getEntries()
                        .stream()
                        .anyMatch(entry -> entry.getId() != null && entry.isSameId(entryId)))
                .findFirst();
    }
}
