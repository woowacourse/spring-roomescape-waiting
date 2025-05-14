package roomescape.dao.fake;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.dao.ReservationDao;
import roomescape.entity.Reservation;

public class FakeReservationDao implements ReservationDao {

    private final List<Reservation> reservations = new ArrayList<>();
    private final AtomicLong index = new AtomicLong(1);

    public List<Reservation> findAllReservations() {
        return Collections.unmodifiableList(reservations);
    }

    public List<Reservation> findReservationsByFilters(Long themeId, Long memberId,
        LocalDate dateFrom, LocalDate dateTo) {
        return reservations.stream()
            .filter(r -> themeId == null || r.getTheme().getId().equals(themeId))
            .filter(r -> memberId == null || r.getMember().getId().equals(memberId))
            .filter(r -> dateFrom == null || !r.getDate().isBefore(dateFrom))
            .filter(r -> dateTo == null || !r.getDate().isAfter(dateTo))
            .toList();
    }

    public boolean existReservationByDateTimeAndTheme(LocalDate date, Long timeId, Long themeId) {
        return reservations.stream()
            .anyMatch(r -> r.getDate().equals(date)
                && r.getTime().getId().equals(timeId)
                && r.getTheme().getId().equals(themeId));
    }

    public Reservation addReservation(Reservation reservation) {
        Reservation newReservation = new Reservation(
            index.getAndIncrement(),
            reservation.getMember(),
            reservation.getDate(),
            reservation.getTime(),
            reservation.getTheme());

        reservations.add(newReservation);
        return newReservation;
    }

    public void removeReservationById(Long id) {
        reservations.removeIf(r -> r.getId().equals(id));
    }
}
