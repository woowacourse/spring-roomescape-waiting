package roomescape.fake;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.repository.ReservationTimeRepository;

public class FakeReservationTimeRepository implements ReservationTimeRepository {

    List<ReservationTime> times = new ArrayList<>();
    Long index = 1L;

    public ReservationTime save(final ReservationTime reservationTime) {
        ReservationTime newReservationTime = new ReservationTime(index++, reservationTime.getStartAt());
        times.add(newReservationTime);
        return newReservationTime;
    }

    public List<ReservationTime> findAll() {
        return times;
    }

    public Optional<ReservationTime> findById(final long id) {
        ReservationTime reservationTime = times.stream()
                .filter(time -> time.getId() == id)
                .findFirst()
                .orElse(null);
        return Optional.ofNullable(reservationTime);
    }

    public boolean existsByTime(final LocalTime reservationTime) {
        return times.stream()
                .anyMatch(time -> time.getStartAt().equals(reservationTime));
    }

    public void deleteById(final long id) {
        ReservationTime reservationTime = findById(id).orElseThrow();
        times.remove(reservationTime);
    }
}
