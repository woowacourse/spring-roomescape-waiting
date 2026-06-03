package roomescape.repository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import roomescape.domain.Reservation;

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
        Reservation savedReservation = new Reservation(id, reservation.getName(), reservation.getDate(),
                reservation.getTimeSlot(),
                reservation.getTheme(),
                reservation.getCreatedAt());
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
                .filter(reservation -> isDuplicate(reservation, date, timeId, themeId))
                .findAny();
    }

    @Override
    public int update(Reservation reservation) {
        if (!storage.containsKey(reservation.getId())) {
            return 0;
        }
        storage.put(reservation.getId(), reservation);
        return 1;
    }

    @Override
    public boolean existsByDateAndTimeAndTheme(LocalDate date, Long timeId, Long themeId) {
        return storage.values().stream()
                .anyMatch(reservation -> isDuplicate(reservation, date, timeId, themeId));
    }

    @Override
    public boolean existsByThemeId(Long themeId) {
        return storage.values().stream()
                .anyMatch(reservation -> reservation.getTheme().getId().equals(themeId));
    }

    @Override
    public boolean existsByTimeId(Long timeId) {
        return storage.values().stream()
                .anyMatch(reservation -> reservation.getTimeSlot().getId().equals(timeId));
    }

    private boolean isDuplicate(Reservation reservation, LocalDate date, Long timeId, Long themeId) {
        return reservation.getDate().equals(date)
                && reservation.getTimeSlot().getId().equals(timeId)
                && reservation.getTheme().getId().equals(themeId);
    }
}
