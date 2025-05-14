package roomescape.repository.fake;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import roomescape.repository.ReservationRepository;
import roomescape.entity.Reservation;

public class FakeReservationRepository implements ReservationRepository {

    private final List<Reservation> reservations = new ArrayList<>();

    public List<Reservation> findAll() {
        return Collections.unmodifiableList(reservations);
    }

    public List<Reservation> findByMemberId(Long memberId) {
        return reservations.stream()
            .filter(reservation -> reservation.getMember().getId().equals(memberId))
            .toList();
    }

    public boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId) {
        return reservations.stream()
            .anyMatch(r -> r.getDate().equals(date)
                && r.getTime().getId().equals(timeId)
                && r.getTheme().getId().equals(themeId));
    }

    public Reservation save(Reservation reservation) {
        Reservation newReservation = new Reservation(
            reservation.getMember(),
            reservation.getDate(),
            reservation.getTime(),
            reservation.getTheme());

        reservations.add(newReservation);
        return newReservation;
    }

    public void deleteById(Long id) {
        reservations.removeIf(r -> r.getId().equals(id));
    }
}
