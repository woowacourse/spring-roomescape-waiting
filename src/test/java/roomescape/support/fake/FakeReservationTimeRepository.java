package roomescape.support.fake;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.ReservationTimeRepository;

public class FakeReservationTimeRepository implements ReservationTimeRepository {

    private final List<ReservationTime> times = new ArrayList<>();
    private Long index = 1L;

    public ReservationTime save(final ReservationTime reservationTime) {
        final ReservationTime newReservationTime = new ReservationTime(index++, reservationTime.getStartAt());
        times.add(newReservationTime);
        return newReservationTime;
    }

    public List<ReservationTime> findAll() {
        return times;
    }

    public Optional<ReservationTime> findById(final long id) {
        final ReservationTime reservationTime = times.stream()
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
        final ReservationTime reservationTime = findById(id).orElseThrow();
        times.remove(reservationTime);
    }
}
