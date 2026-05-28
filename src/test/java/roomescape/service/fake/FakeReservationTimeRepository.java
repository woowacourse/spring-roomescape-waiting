package roomescape.service.fake;

import java.time.LocalTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.domain.ReservationTime;
import roomescape.repository.ReservationTimeRepository;

public class FakeReservationTimeRepository implements ReservationTimeRepository {

    private final Map<Long, ReservationTime> reservationTimes = new ConcurrentHashMap<>();
    private final AtomicLong counter = new AtomicLong(1);

    @Override
    public ReservationTime save(ReservationTime reservationTime) {
        ReservationTime saved = new ReservationTime(
                counter.getAndIncrement(),
                reservationTime.getStartAt(),
                reservationTime.getStatus()
        );
        reservationTimes.put(saved.getId(), saved);
        return saved;
    }

    @Override
    public Optional<ReservationTime> findById(long id) {
        return reservationTimes.values()
                .stream()
                .filter(time -> time.getId().equals(id))
                .findFirst();
    }

    @Override
    public boolean existsByStartAt(LocalTime time) {
        return reservationTimes.values()
                .stream()
                .anyMatch(reservationTime -> reservationTime.getStartAt().equals(time));
    }

    @Override
    public void update(ReservationTime time) {
        reservationTimes.put(time.getId(), time);
    }

    public ReservationTime get(long id) {
        return reservationTimes.get(id);
    }
}
