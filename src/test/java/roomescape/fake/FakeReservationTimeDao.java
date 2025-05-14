package roomescape.fake;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import roomescape.time.domain.ReservationTime;
import roomescape.time.repository.ReservationTimeRepository;

public class FakeReservationTimeDao implements ReservationTimeRepository {

    List<ReservationTime> times = new ArrayList<>();
    Long index = 1L;

    @Override
    public ReservationTime save(final ReservationTime reservationTime) {
        ReservationTime newReservationTime = new ReservationTime(index++, reservationTime.getStartAt());
        times.add(newReservationTime);
        return newReservationTime;
    }

    @Override
    public <S extends ReservationTime> Iterable<S> saveAll(final Iterable<S> entities) {
        return null;
    }

    @Override
    public Optional<ReservationTime> findById(final Long aLong) {
        return times.stream()
                .filter(time -> time.getId() == aLong)
                .findFirst();
    }

    @Override
    public boolean existsById(final Long aLong) {
        return false;
    }

    @Override
    public List<ReservationTime> findAll() {
        return times;
    }

    @Override
    public Iterable<ReservationTime> findAllById(final Iterable<Long> longs) {
        return null;
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void deleteById(final Long aLong) {

    }

    @Override
    public void delete(final ReservationTime entity) {

    }

    @Override
    public void deleteAllById(final Iterable<? extends Long> longs) {

    }

    @Override
    public void deleteAll(final Iterable<? extends ReservationTime> entities) {

    }

    @Override
    public void deleteAll() {

    }

    @Override
    public boolean existsByStartAt(final LocalTime reservationTime) {
        return times.stream()
                .anyMatch(time -> time.getStartAt().equals(reservationTime));
    }
}
