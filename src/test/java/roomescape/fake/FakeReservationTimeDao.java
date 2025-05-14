package roomescape.fake;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.dao.ReservationTimeDao;
import roomescape.model.AvailableReservationTime;
import roomescape.model.ReservationTime;

public class FakeReservationTimeDao implements ReservationTimeDao {

    private final List<ReservationTime> times = new ArrayList<>();
    private final AtomicLong nextId = new AtomicLong(1L);

    @Override
    public List<ReservationTime> findAll() {
        return new ArrayList<>(times);
    }

    @Override
    public Long saveTime(ReservationTime reservationTime) {
        ReservationTime newTime = new ReservationTime(nextId.getAndIncrement(), reservationTime.getStartAt());
        times.add(newTime);
        return newTime.getId();
    }

    @Override
    public void deleteTimeById(Long id) {
        times.removeIf(time -> time.getId().equals(id));
    }

    @Override
    public Optional<ReservationTime> findById(Long id) {
        return times.stream()
                .filter(time -> time.getId().equals(id))
                .findFirst();
    }

    @Override
    public boolean isDuplicatedStartAtExisted(LocalTime startAt) {
        return times.stream().anyMatch(time -> time.getStartAt().equals(startAt));
    }

    @Override
    public List<AvailableReservationTime> findAvailableTimes(String date, Long themeId) {
        return List.of();
    }
}
