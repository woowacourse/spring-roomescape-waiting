package roomescape.fake;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.dao.ReservationTimeDao;
import roomescape.domain.ReservationTime;

public class FakeReservationTimeDao implements ReservationTimeDao {

    private final List<ReservationTime> times = new ArrayList<>();
    private final AtomicLong index = new AtomicLong(1);

    @Override
    public ReservationTime add(ReservationTime reservationTime) {
        ReservationTime saved = new ReservationTime(
                index.getAndIncrement(),
                reservationTime.getStartAt()
        );
        times.add(saved);
        return saved;
    }

    @Override
    public List<ReservationTime> findAll() {
        return new ArrayList<>(times);
    }

    @Override
    public ReservationTime findById(Long id) {
        return times.stream()
            .filter(time -> time.getId().equals(id))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("예약 시간을 찾을 수 없습니다."));
    }

    @Override
    public int deleteById(Long id) {
        Optional<ReservationTime> deleteReservationTime = times.stream()
                .filter(reservationTime -> reservationTime.getId().equals(id))
                .findFirst();
        if (deleteReservationTime.isPresent()) {
            times.remove(deleteReservationTime.get());
            return 1;
        }
        return 0;
    }

    @Override
    public boolean existByStartAt(LocalTime startAt) {
        return times.stream()
            .anyMatch(time -> time.getStartAt().equals(startAt));
    }
}
