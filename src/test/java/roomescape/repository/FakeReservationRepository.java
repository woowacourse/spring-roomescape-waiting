package roomescape.repository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;

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
    public List<Reservation> findBySlotId(long slotId) {
        return storage.values().stream()
                .filter(reservation -> Objects.equals(reservation.getSlot().getId(), slotId))
                .toList();
    }

    @Override
    public List<Reservation> findBySlotIds(List<Long> slotIds) {
        return storage.values().stream()
                .filter(reservation -> slotIds.contains(reservation.getSlot().getId()))
                .toList();
    }

    @Override
    public Reservation save(Reservation reservation) {
        long id = sequence++;
        Reservation savedReservation = new Reservation(
                id,
                reservation.getName(),
                reservation.getSlot(),
                reservation.getCreatedAt(),
                reservation.getStatus()
        );
        storage.put(id, savedReservation);
        return savedReservation;
    }

    @Override
    public void deleteById(long id) {
        storage.remove(id);
    }

    @Override
    public Optional<Reservation> findReservedBySlot(LocalDate date, long timeId, long themeId) {
        return storage.values().stream()
                .filter(Reservation::isReserved)
                .filter(reservation -> hasSameSlot(reservation, date, timeId, themeId))
                .findAny();
    }

    @Override
    public void update(Reservation reservation) {
        if (!storage.containsKey(reservation.getId())) {
            return;
        }
        storage.put(reservation.getId(), reservation);
    }

    @Override
    public boolean existsByThemeId(long themeId) {
        return storage.values().stream()
                .anyMatch(reservation -> reservation.getTheme().getId().equals(themeId));
    }

    @Override
    public boolean existsByTimeId(long timeId) {
        return storage.values().stream()
                .anyMatch(reservation -> reservation.getTimeSlot().getId().equals(timeId));
    }

    private boolean hasSameSlot(Reservation reservation, LocalDate date, Long timeId, Long themeId) {
        return reservation.getDate().equals(date)
                && reservation.getTimeSlot().getId().equals(timeId)
                && reservation.getTheme().getId().equals(themeId);
    }
}
