package roomescape.repository;

import roomescape.domain.Reservation;
import roomescape.domain.Session;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FakeReservationRepository implements ReservationRepository {

    private final Map<Long, Reservation> storage = new HashMap<>();
    private long sequence = 1L;

    @Override
    public List<Reservation> findAll() {
        return List.copyOf(storage.values());
    }

    @Override
    public Optional<Reservation> findById(long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Reservation> findByName(String name) {
        return storage.values().stream()
                .filter(reservation -> reservation.getName().equals(name))
                .toList();
    }

    @Override
    public Reservation save(Reservation reservation) {
        long id = sequence++;
        Reservation savedReservation = new Reservation(id, reservation.getName(), reservation.getSession(), reservation.getAmount(), reservation.getPaymentKey());
        storage.put(id, savedReservation);
        return savedReservation;
    }

    @Override
    public void deleteById(long id) {
        storage.remove(id);
    }

    @Override
    public Optional<Reservation> findByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId) {
        return storage.values().stream()
                .filter(reservation -> matchSlot(reservation.getSession(), date, timeId, themeId))
                .findAny();
    }

    @Override
    public Reservation update(Reservation reservation) {
        if (!storage.containsKey(reservation.getId())) {
            return null;
        }
        storage.put(reservation.getId(), reservation);
        return reservation;
    }

    private boolean matchSlot(Session session, LocalDate date, Long timeId, Long themeId) {
        return session.getDate().equals(date)
                && session.getTimeSlot().getId().equals(timeId)
                && session.getTheme().getId().equals(themeId);
    }
}
