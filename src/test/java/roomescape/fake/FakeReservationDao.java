package roomescape.fake;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;

public class FakeReservationDao implements ReservationRepository {

    List<Reservation> reservations = new ArrayList<>();
    Long index = 1L;

    @Override
    public Reservation save(final Reservation reservation) {
        Reservation newReservation = reservation.withId(index++);
        reservations.add(newReservation);
        return newReservation;
    }

    @Override
    public List<Reservation> findAll() {
        return reservations;
    }

    @Override
    public List<Reservation> findAll(
            final Long memberId,
            final Long themeId,
            final LocalDate fromDate,
            final LocalDate toDate
    ) {
        return List.of();
    }

    @Override
    public void deleteById(final long id) {
        Reservation reservation = findById(id);
        reservations.remove(reservation);
    }

    @Override
    public boolean existsByTimeId(final long timeId) {
        return reservations.stream()
                .anyMatch(reservation -> reservation.getDateTime().getTimeId() == timeId);
    }

    @Override
    public boolean existsByThemeId(Long themeId) {
        return reservations.stream()
                .anyMatch(reservation -> reservation.getTheme().getId() == themeId);
    }

    @Override
    public boolean existsByDateAndTimeAndTheme(final LocalDate date, final long timeId, final long themeId) {
        return reservations.stream()
                .anyMatch(reservation ->
                        reservation.getTheme().getId() == themeId
                                && reservation.getDateTime().getDate().isEqual(date)
                                && reservation.getDateTime().getTimeId() == timeId);
    }

    public Reservation findById(final long id) {
        return reservations.stream()
                .filter(reservation -> reservation.getId() == id)
                .findFirst()
                .orElseThrow();
    }
}
