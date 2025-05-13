package roomescape.infrastructure.fake;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.domain.Reservation;
import roomescape.domain.repository.ReservationRepository;
import roomescape.domain.repository.ReservationSearchFilter;

public class ReservationFakeRepository implements ReservationRepository {

    private final Map<Long, Reservation> reservations = new ConcurrentHashMap<>();
    private final AtomicLong index = new AtomicLong(1L);

    @Override
    public Optional<Reservation> findById(final long id) {
        return Optional.ofNullable(reservations.get(id));
    }

    @Override
    public long save(final Reservation reservation) {
        var id = index.getAndIncrement();
        var created = Reservation.ofExisting(
            id,
            reservation.user(),
            reservation.date(),
            reservation.timeSlot(),
            reservation.theme()
        );
        reservations.put(id, created);
        return id;
    }

    @Override
    public boolean removeById(final long id) {
        Reservation removed = reservations.remove(id);
        return removed != null;
    }

    @Override
    public List<Reservation> findAll() {
        return List.copyOf(reservations.values());
    }

    @Override
    public List<Reservation> findByTimeSlotId(final long id) {
        return reservations.values().stream()
            .filter(reservation -> reservation.timeSlot().id() == id)
            .toList();
    }

    @Override
    public List<Reservation> findByThemeId(final long id) {
        return reservations.values().stream()
            .filter(reservation -> reservation.theme().id() == id)
            .toList();
    }

    @Override
    public List<Reservation> findBySearchFilter(final ReservationSearchFilter filter) {
        var reservations = new ArrayList<>(this.reservations.values());
        if (filter.themeId() != null) {
            reservations.removeIf(reservation -> !reservation.theme().id().equals(filter.themeId()));
        }
        if (filter.userId() != null) {
            reservations.removeIf(reservation -> !reservation.user().id().equals(filter.userId()));
        }
        if (filter.dateFrom() != null) {
            reservations.removeIf(reservation -> reservation.date().isBefore(filter.dateFrom()));
        }
        if (filter.dateTo() != null) {
            reservations.removeIf(reservation -> reservation.date().isAfter(filter.dateTo()));
        }
        return List.copyOf(reservations);
    }

    @Override
    public List<Reservation> findByDateAndThemeId(final LocalDate date, final long themeId) {
        return reservations.values().stream()
            .filter(r -> r.date().equals(date) && r.theme().id() == themeId)
            .toList();
    }

    @Override
    public Optional<Reservation> findByDateAndTimeSlotAndThemeId(final LocalDate date, final long timeSlotId, final long themeId) {
        return reservations.values().stream()
            .filter(r -> r.date().equals(date) && r.timeSlot().id() == timeSlotId && r.theme().id() == themeId)
            .findAny();
    }
}
