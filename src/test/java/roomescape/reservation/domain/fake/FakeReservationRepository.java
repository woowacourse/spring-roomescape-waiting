package roomescape.reservation.domain.fake;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.domain.Status;

public class FakeReservationRepository implements ReservationRepository {

    private final List<Reservation> reservations = new CopyOnWriteArrayList<>();
    private final AtomicLong counter = new AtomicLong(1);

    @Override
    public Reservation save(Reservation reservation) {
        Reservation saved = Reservation.restore(counter.getAndIncrement(), reservation.getName(), reservation.getDate(),
                reservation.getTime(), reservation.getTheme(), reservation.getStatus(), reservation.getCreatedAt());
        reservations.add(saved);
        return saved;
    }

    @Override
    public void update(Reservation reservation) {
        for (int i = 0; i < reservations.size(); i++) {
            if (reservations.get(i).getId().equals(reservation.getId())) {
                reservations.set(i, reservation);
                return;
            }
        }
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        return reservations.stream()
                .filter(reservation -> reservation.getId().equals(id))
                .filter(reservation -> reservation.getStatus() != Status.CANCELED)
                .findFirst();
    }

    @Override
    public Optional<Reservation> findNextWaitingReservation(LocalDate date, Long timeId, Long themeId) {
        return reservations.stream()
                .filter(reservation -> reservation.getDate().equals(date))
                .filter(reservation -> reservation.getTime().getId().equals(timeId))
                .filter(reservation -> reservation.getTheme().getId().equals(themeId))
                .filter(reservation -> reservation.getStatus() == Status.WAITING)
                .min(Comparator.comparing(Reservation::getCreatedAt));
    }

    @Override
    public List<Reservation> findAll(int page, int size) {
        return reservations.stream()
                .filter(reservation -> reservation.getStatus() == Status.RESERVED)
                .sorted(Comparator.comparing(Reservation::getDate).thenComparing(reservation -> reservation.getTime().getStartAt()))
                .skip((long) page * size)
                .limit(size)
                .toList();
    }

    @Override
    public List<Reservation> findByThemeAndDate(Long themeId, LocalDate date) {
        return reservations.stream()
                .filter(reservation -> reservation.getTheme().getId().equals(themeId))
                .filter(reservation -> reservation.getDate().equals(date))
                .filter(reservation -> reservation.getStatus() == Status.RESERVED)
                .toList();
    }

    @Override
    public List<Reservation> findAllByName(String username) {
        return reservations.stream()
                .filter(reservation -> reservation.getName().equals(username))
                .filter(reservation -> reservation.getStatus() != Status.CANCELED)
                .toList();
    }

    @Override
    public Long countWaitingBefore(Reservation reservation) {
        return reservations.stream()
                .filter(waiting -> waiting.getStatus() == Status.WAITING)
                .filter(waiting -> waiting.getDate().equals(reservation.getDate()))
                .filter(waiting -> waiting.getTime().getId().equals(reservation.getTime().getId()))
                .filter(waiting -> waiting.getTheme().getId().equals(reservation.getTheme().getId()))
                .filter(waiting -> waiting.getCreatedAt().isBefore(reservation.getCreatedAt())
                        || (waiting.getCreatedAt().equals(reservation.getCreatedAt())
                        && waiting.getId() < reservation.getId()))
                .count();
    }

    @Override
    public boolean existsByReservationTime(Long timeId) {
        return reservations.stream()
                .anyMatch(reservation -> reservation.getTime().getId().equals(timeId)
                        && reservation.getStatus() == Status.RESERVED);
    }

    @Override
    public boolean existsByTheme(Long themeId) {
        return reservations.stream()
                .anyMatch(reservation -> reservation.getTheme().getId().equals(themeId)
                        && reservation.getStatus() != Status.CANCELED);
    }

    @Override
    public boolean existsActiveReservationByDateTimeAndTheme(Long timeId, Long themeId, LocalDate date) {
        return reservations.stream()
                .anyMatch(reservation -> reservation.getDate().equals(date)
                        && reservation.getTime().getId().equals(timeId)
                        && reservation.getTheme().getId().equals(themeId)
                        && reservation.getStatus() == Status.RESERVED);
    }

    @Override
    public boolean existsByUsernameAndDateTimeAndTheme(Long timeId, Long themeId, LocalDate date, String name) {
        return reservations.stream()
                .anyMatch(reservation -> reservation.getDate().equals(date)
                        && reservation.getTime().getId().equals(timeId)
                        && reservation.getTheme().getId().equals(themeId)
                        && reservation.getName().equals(name)
                        && reservation.getStatus() != Status.CANCELED);
    }
}
