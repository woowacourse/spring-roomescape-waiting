package roomescape.support;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationslot.ReservationSlot;
import roomescape.repository.reservation.ReservationRepository;

public class FakeReservationRepository implements ReservationRepository {

    private final Map<Long, Reservation> reservations = new LinkedHashMap<>();
    private long sequence = 1L;

    @Override
    public List<Reservation> findAll() {
        return new ArrayList<>(reservations.values());
    }

    @Override
    public Optional<Reservation> findById(final long id) {
        return Optional.ofNullable(reservations.get(id));
    }

    @Override
    public Optional<Reservation> findBySlot(final ReservationSlot slot) {
        return reservations.values()
                .stream()
                .filter(reservation -> Objects.equals(reservation.getDate(), slot.getDate()))
                .filter(reservation -> Objects.equals(reservation.getTheme(), slot.getTheme()))
                .filter(reservation -> Objects.equals(reservation.getTime(), slot.getTime()))
                .findFirst();
    }

    @Override
    public Reservation save(final Reservation reservation) {
        Reservation saved = reservation;
        if (saved.getId() == null) {
            saved = saved.withId(sequence++);
        }

        reservations.put(saved.getId(), saved);
        return saved;
    }

    @Override
    public void delete(final Reservation reservation) {
        reservations.remove(reservation.getId());
    }
}
