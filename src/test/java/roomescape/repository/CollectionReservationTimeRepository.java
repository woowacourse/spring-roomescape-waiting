package roomescape.repository;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationTimes;

public class CollectionReservationTimeRepository implements ReservationTimeRepository {
    private final List<ReservationTime> reservationTimes;
    private final AtomicLong atomicLong;

    public CollectionReservationTimeRepository() {
        this(new ArrayList<>());
    }

    public CollectionReservationTimeRepository(List<ReservationTime> reservationTimes) {
        this(reservationTimes, new AtomicLong(0));
    }

    public CollectionReservationTimeRepository(List<ReservationTime> reservationTimes, AtomicLong atomicLong) {
        this.reservationTimes = reservationTimes;
        this.atomicLong = atomicLong;
    }

    @Override
    public ReservationTime save(ReservationTime reservationTime) {
        ReservationTime saved = new ReservationTime(atomicLong.incrementAndGet(), reservationTime.getStartAt());
        reservationTimes.add(saved);
        return saved;
    }

    @Override
    public boolean existsByStartAt(LocalTime startAt) {
        return reservationTimes.stream()
                .anyMatch(reservationTime -> startAt.equals(reservationTime.getStartAt()));
    }

    @Override
    public Optional<ReservationTime> findById(long id) {
        return reservationTimes.stream()
                .filter(reservationTime -> reservationTime.isIdOf(id))
                .findFirst();
    }

    @Override
    public ReservationTimes findAll() {
        return new ReservationTimes(reservationTimes);
    }

    @Override
    public void delete(long id) {
        reservationTimes.stream()
                .filter(reservationTime -> reservationTime.isIdOf(id))
                .findAny()
                .ifPresent(reservationTimes::remove);
    }
}
