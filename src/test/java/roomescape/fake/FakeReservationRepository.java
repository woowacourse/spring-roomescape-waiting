package roomescape.fake;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;

public class FakeReservationRepository implements ReservationRepository {

    List<Reservation> reservations = new ArrayList<>();
    Long index = 1L;

    public Reservation save(final Reservation reservation) {
        Reservation newReservation = new Reservation(index++, reservation.getMember(), reservation.getDate(),
                reservation.getTime(), reservation.getTheme());
        reservations.add(newReservation);
        return newReservation;
    }

    public List<Reservation> findAll() {
        return reservations;
    }

    public void deleteById(final long id) {
        Reservation reservation = findById(id);
        reservations.remove(reservation);
    }

    public boolean existsByDateAndTimeIdAndThemeId(final LocalDate date, final long timeId, final long themeId) {
        return reservations.stream()
                .anyMatch(reservation -> reservation.getDate().equals(date) &&
                        reservation.getTime().getId() == timeId &&
                        reservation.getTheme().getId() == themeId);
    }

    public boolean existsByTimeId(final long timeId) {
        return reservations.stream()
                .anyMatch(reservation -> reservation.getTime().getId() == timeId);
    }

    public boolean existsByThemeId(Long themeId) {
        return reservations.stream()
                .anyMatch(reservation -> reservation.getTheme().getId() == themeId);
    }

    public List<Reservation> findAllByDateAndThemeId(final LocalDate date, final long themeId) {
        return reservations.stream()
                .filter(reservation -> reservation.getDate().equals(date))
                .filter(reservation -> reservation.getTheme().getId() == themeId)
                .toList();
    }

    public Reservation findById(final long id) {
        return reservations.stream()
                .filter(reservation -> reservation.getId() == id)
                .findFirst()
                .orElseThrow();
    }
}
