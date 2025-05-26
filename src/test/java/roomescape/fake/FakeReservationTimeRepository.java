package roomescape.fake;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.repository.time.ReservationTimeRepositoryInterface;

public class FakeReservationTimeRepository implements ReservationTimeRepositoryInterface {

    private final Map<Long, ReservationTime> reservationTimes = new HashMap<>();

    private long sequence = 0;

    @Override
    public ReservationTime save(final ReservationTime reservationTime) {
        sequence++;
        ReservationTime newReservationTime = new ReservationTime(sequence, reservationTime.getStartAt());
        reservationTimes.put(sequence, newReservationTime);
        return newReservationTime;
    }

    @Override
    public boolean existsByStartAt(final LocalTime startAt) {
        return reservationTimes.values().stream()
                .anyMatch(reservationTime -> reservationTime.getStartAt().equals(startAt));
    }

    @Override
    public ReservationTime findById(final Long id) {
        return reservationTimes.get(id);
    }

    @Override
    public void deleteById(final Long id) {
        reservationTimes.remove(id);
    }

    @Override
    public List<ReservationTime> findAll() {
        return List.copyOf(reservationTimes.values());
    }
}
