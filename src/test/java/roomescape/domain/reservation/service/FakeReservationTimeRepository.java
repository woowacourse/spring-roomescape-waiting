package roomescape.domain.reservation.service;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.domain.reservation.domain.reservationTime.ReservationTime;
import roomescape.domain.reservation.repository.ReservationTimeRepository;

public class FakeReservationTimeRepository implements ReservationTimeRepository {

    public Map<Long, ReservationTime> reservationTimes;
    AtomicLong atomicLong = new AtomicLong(0);

    public FakeReservationTimeRepository() {
        this.reservationTimes = new HashMap<>();
    }

    public FakeReservationTimeRepository(List<ReservationTime> reservationsTimes) {
        this.reservationTimes = new HashMap<>();
        for (int i = 0; i < reservationsTimes.size(); i++) {
            this.reservationTimes.put(reservationsTimes.get(i).getId(), reservationsTimes.get(i));
        }
    }

    @Override
    public List<ReservationTime> findAll() {
        return reservationTimes.values().stream().toList();
    }

    @Override
    public Optional<ReservationTime> findById(Long id) {
        if (reservationTimes.containsKey(id)) {
            return Optional.of(reservationTimes.get(id));
        }
        return Optional.empty();
    }

    @Override
    public boolean existByStartAt(LocalTime startAt) {
        return reservationTimes.values().stream()
                .anyMatch((reservationTime -> reservationTime.getStartAt().equals(startAt)));
    }

    @Override
    public ReservationTime insert(ReservationTime reservationTime) {
        Long id = atomicLong.incrementAndGet();
        ReservationTime addReservationTime = new ReservationTime(id, reservationTime.getStartAt());
        reservationTimes.put(id, addReservationTime);
        return addReservationTime;
    }

    @Override
    public void deleteById(Long id) {
        reservationTimes.remove(id);
    }
}
