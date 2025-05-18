package roomescape.support.fake;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import roomescape.reservation.domain.time.ReservationTime;
import roomescape.reservation.domain.time.ReservationTimeRepository;

public class FakeReservationTimeRepository implements ReservationTimeRepository {

    private final List<ReservationTime> times = new ArrayList<>();
    private Long index = 1L;

    @Override
    public boolean existsByStartAt(final LocalTime startAt) {
        return times.stream()
                .anyMatch(time -> time.startAt().equals(startAt));
    }

    @Override
    public ReservationTime save(final ReservationTime reservationTime) {
        final ReservationTime newReservationTime = new ReservationTime(index++, reservationTime.startAt());
        times.add(newReservationTime);
        return newReservationTime;
    }

    @Override
    public void deleteById(final long id) {
        final ReservationTime reservationTime = findById(id).orElseThrow();
        times.remove(reservationTime);
    }

    @Override
    public List<ReservationTime> findAll() {
        return times;
    }

    @Override
    public Optional<ReservationTime> findById(final long id) {
        final ReservationTime reservationTime = times.stream()
                .filter(time -> time.id() == id)
                .findFirst()
                .orElse(null);
        return Optional.ofNullable(reservationTime);
    }
}
