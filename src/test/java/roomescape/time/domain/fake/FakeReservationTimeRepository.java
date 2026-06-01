package roomescape.time.domain.fake;

import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.time.domain.ReservationTime;
import roomescape.time.domain.ReservationTimeRepository;

public class FakeReservationTimeRepository implements ReservationTimeRepository {

    private final List<ReservationTime> reservationTimes = new CopyOnWriteArrayList<>();
    private final AtomicLong counter = new AtomicLong(1);

    @Override
    public ReservationTime save(ReservationTime reservationTime) {
        ReservationTime saved = ReservationTime.restore(counter.getAndIncrement(), reservationTime.getStartAt(),
                reservationTime.isActive());
        reservationTimes.add(saved);
        return saved;
    }

    @Override
    public List<ReservationTime> findAll() {
        return reservationTimes.stream()
                .filter(ReservationTime::isActive)
                .sorted(Comparator.comparing(ReservationTime::getStartAt))
                .toList();
    }

    @Override
    public Optional<ReservationTime> findById(Long id) {
        return reservationTimes.stream().filter(time -> time.getId().equals(id)).findFirst();
    }

    @Override
    public boolean existsActiveByStartAt(LocalTime time) {
        return reservationTimes.stream()
                .anyMatch(reservationTime -> reservationTime.getStartAt().equals(time) && reservationTime.isActive());
    }

    @Override
    public void update(ReservationTime time) {
        for (int i = 0; i < reservationTimes.size(); i++) {
            if (reservationTimes.get(i).getId().equals(time.getId())) {
                reservationTimes.set(i, time);
                return;
            }
        }
    }
}
