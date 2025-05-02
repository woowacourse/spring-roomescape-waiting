package roomescape.unit.repository.reservation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.domain.reservation.Reservation;
import roomescape.exception.reservation.InvalidReservationException;
import roomescape.repository.reservation.ReservationRepository;

public class FakeReservationRepository implements ReservationRepository {

    private final AtomicLong index = new AtomicLong(1L);
    private final List<Reservation> reservations = new ArrayList<>();

    @Override
    public long add(Reservation reservation) {
        long id = index.getAndIncrement();
        reservations.add(
                new Reservation(id, reservation.getName(), reservation.getDate(), reservation.getReservationTime(),
                        reservation.getTheme()));
        return id;
    }

    @Override
    public List<Reservation> findAll() {
        return Collections.unmodifiableList(reservations);
    }

    @Override
    public void deleteById(Long id) {
        Optional<Reservation> findReservation = reservations.stream()
                .filter(reservation -> reservation.getId().equals(id))
                .findAny();
        findReservation.orElseThrow(() -> new InvalidReservationException("존재하지 않는 id입니다." + id));
        reservations.remove(findReservation.get());
    }

    @Override
    public boolean existsByTimeId(Long id) {
        return reservations.stream()
                .anyMatch(reservation -> reservation.getReservationTime().getId().equals(id));
    }

    @Override
    public boolean existsByDateAndTimeIdAndTheme(Reservation reservation) {
        return reservations.stream()
                .anyMatch(currentReservation -> currentReservation.getDate().isEqual(reservation.getDate())
                        && currentReservation.getReservationTime().getId()
                        .equals(reservation.getReservationTime().getId())
                        && currentReservation.getTheme().getId()
                        .equals(reservation.getTheme().getId()));
    }

    @Override
    public boolean existsByThemeId(long id) {
        return reservations.stream()
                .anyMatch((reservation) -> reservation.getTheme().getId().equals(id));
    }

    @Override
    public List<Reservation> findAllByDateAndThemeId(LocalDate date, Long themeId) {
        return reservations.stream()
                .filter((reservation) -> reservation.getDate().isEqual(date))
                .filter((reservation) -> reservation.getTheme().getId().equals(themeId))
                .toList();
    }

    @Override
    public Optional<Reservation> findById(long id) {
        return reservations.stream()
                .filter((reservation) -> reservation.getId().equals(id))
                .findAny();
    }

    @Override
    public List<Reservation> findAllByDateInRange(LocalDate start, LocalDate end) {
        List<Reservation> reservationsInRange = new ArrayList<>();
        for (Reservation reservation : reservations) {
            if (reservation.getDate().isAfter(start) && reservation.getDate().isBefore(end)) {
                reservationsInRange.add(reservation);
            }
        }
        return reservationsInRange;
    }
}
