package roomescape.reservationTime.infrastructure;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.reservationTime.domain.ReservationTimeRepository;

public class FakeReservationTimeRepository implements ReservationTimeRepository {
    private final List<ReservationTime> reservationTimes;
    private AtomicLong index = new AtomicLong(0);

    public FakeReservationTimeRepository(List<ReservationTime> reservationTimes) {
        this.reservationTimes = reservationTimes;
    }

    @Override
    public ReservationTime save(ReservationTime reservationTime) {
        long currentIndex = index.incrementAndGet();
        reservationTimes.add(ReservationTime.createWithId(currentIndex, reservationTime.getStartAt()));
        return reservationTime.assignId(currentIndex);
    }

    @Override
    public Optional<ReservationTime> findById(Long id) {
        return reservationTimes.stream()
                .filter(reservationTime -> Objects.equals(reservationTime.getId(), id))
                .findAny();
    }

    @Override
    public List<ReservationTime> findAll() {
        return Collections.unmodifiableList(reservationTimes);
    }

    @Override
    public void deleteById(Long id) {
        Optional<ReservationTime> findReservationTime = reservationTimes.stream()
                .filter(reservation -> Objects.equals(reservation.getId(), id))
                .findAny();

        ReservationTime reservationTime = findReservationTime.get();
        reservationTimes.remove(reservationTime);
    }
}
