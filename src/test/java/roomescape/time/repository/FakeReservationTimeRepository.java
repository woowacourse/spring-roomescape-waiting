package roomescape.time.repository;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.common.exception.NotFoundException;
import roomescape.time.domain.ReservationTime;
import roomescape.time.domain.ReservationTimeId;

public class FakeReservationTimeRepository implements ReservationTimeRepository {

    private final List<ReservationTime> reservationTimes = new CopyOnWriteArrayList<>();
    private final AtomicLong index = new AtomicLong(1L);

    @Override
    public boolean existsByStartAt(LocalTime startAt) {
        return reservationTimes.stream()
                .anyMatch(reservationTime -> Objects.equals(reservationTime.getValue(), startAt));
    }

    @Override
    public Optional<ReservationTime> findById(ReservationTimeId id) {
        try {
            return Optional.of(reservationTimes.get((int) (id.getValue() - 1)));
        } catch (IndexOutOfBoundsException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<ReservationTime> findAll() {
        return new ArrayList<>(reservationTimes);
    }

    @Override
    public ReservationTime save(ReservationTime reservationTime) {
        ReservationTime savedReservationTime = ReservationTime.withId(
                ReservationTimeId.from(index.getAndIncrement()),
                reservationTime.getValue());

        reservationTimes.add(savedReservationTime);
        return savedReservationTime;
    }

    @Override
    public void deleteById(ReservationTimeId id) {
        ReservationTime targetReservationTime = reservationTimes.stream()
                .filter(time -> Objects.equals(time.getId(), id))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("데이터베이스에 해당 id가 존재하지 않습니다."));

        reservationTimes.remove(targetReservationTime);
    }
}
