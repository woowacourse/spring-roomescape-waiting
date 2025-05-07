package roomescape.fake;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.dao.ReservationDao;
import roomescape.domain.Reservation;

public class FakeReservationDao implements ReservationDao {

    private final List<Reservation> reservations = new ArrayList<>();
    private final AtomicLong index = new AtomicLong(1);

    @Override
    public Reservation add(Reservation reservation) {
        Reservation saved = new Reservation(
                index.getAndIncrement(),
                reservation.getName(),
                reservation.getDate(),
                reservation.getTime(),
                reservation.getTheme()
        );
        reservations.add(saved);
        return saved;
    }

    @Override
    public List<Reservation> findAll() {
        return new ArrayList<>(reservations);
    }

    @Override
    public List<Reservation> findByDateAndThemeId(LocalDate date, Long themeId) {
        return reservations.stream()
                .filter(reservation -> reservation.getDate().isEqual(date) && reservation.getTheme().getId().equals(themeId))
                .toList();
    }

    @Override
    public int deleteById(Long id) {
        Optional<Reservation> deleteReservation = reservations.stream()
                .filter(reservation -> reservation.getId().equals(id))
                .findFirst();
        if (deleteReservation.isPresent()) {
            reservations.remove(deleteReservation.get());
            return 1;
        }
        return 0;
    }

    @Override
    public boolean existByDateTimeAndTheme(LocalDate date, Long timeId, Long themeId) {
        return reservations.stream()
            .anyMatch(reservation -> reservation.getDate().isEqual(date)
                && reservation.getTime().getId().equals(timeId)
                && reservation.getTime().getId().equals(themeId));
    }
}
