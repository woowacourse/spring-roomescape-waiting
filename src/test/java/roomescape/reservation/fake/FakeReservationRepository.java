package roomescape.reservation.fake;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;

public class FakeReservationRepository implements ReservationRepository {

    private final Map<Long, Reservation> store = new HashMap<>();
    private Long sequence = 1L;

    @Override
    public Reservation save(Reservation reservation) {
        if (reservation.getId() == null) {
            Reservation saved = Reservation.createRow(
                    sequence++,
                    reservation.getName(),
                    reservation.getDate(),
                    reservation.getTime(),
                    reservation.getTheme()
            );
            store.put(saved.getId(), saved);
            return saved;
        }

        store.put(reservation.getId(), reservation);
        return reservation;
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Optional<Reservation> findByDateAndTimeIdAndThemeId(
            LocalDate date,
            Long timeId,
            Long themeId
    ) {
        return store.values().stream()
                .filter(reservation -> reservation.getDate().equals(date))
                .filter(reservation -> reservation.getTime().getId().equals(timeId))
                .filter(reservation -> reservation.getTheme().getId().equals(themeId))
                .findFirst();
    }

    @Override
    public List<Reservation> findAll() {
        return store.values().stream().toList();
    }

    @Override
    public boolean existsByReservationTimeId(Long reservationTimeId) {
        return store.values().stream()
                .anyMatch(reservation -> reservation.getTime()
                        .getId()
                        .equals(reservationTimeId)
                );
    }

    @Override
    public boolean existsByThemeId(Long themeId) {
        return store.values().stream()
                .anyMatch(reservation -> reservation.getTheme()
                        .getId()
                        .equals(themeId)
                );
    }

    @Override
    public void updateSchedule(Reservation updateReservation) {
        Reservation reservation = store.get(updateReservation.getId());
        if (reservation == null) {
            return;
        }
        if (!reservation.getName().equals(updateReservation.getName())) {
            return;
        }
        store.put(updateReservation.getId(), updateReservation);
    }

    @Override
    public List<Reservation> findByDateAndThemeId(LocalDate date, Long themeId) {
        return store.values().stream()
                .filter(reservation -> date == null || reservation.getDate().equals(date))
                .filter(reservation -> themeId == null || reservation.getTheme().getId().equals(themeId))
                .toList();
    }

    @Override
    public List<Reservation> findByName(String name) {
        return store.values().stream()
                .filter(reservation -> reservation.getName().equals(name))
                .toList();
    }

    @Override
    public void deleteById(Long id) {
        store.remove(id);
    }

    @Override
    public void deleteByIdAndName(Long id, String name) {
        store.values().removeIf(reservation ->
                reservation.getId().equals(id)
                        && reservation.getName().equals(name)
        );
    }
}
