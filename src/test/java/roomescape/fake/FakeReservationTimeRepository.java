package roomescape.fake;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.repository.ReservationTimeRepository;

public class FakeReservationTimeRepository implements ReservationTimeRepository {

    private final Map<Long, ReservationTime> reservationTimes = new HashMap<>();

    private long sequence = 0;

    @Override
    public <S extends ReservationTime> S save(S entity) {
        sequence++;
        ReservationTime reservationTime = new ReservationTime(sequence, entity.getStartAt());
        reservationTimes.put(sequence, reservationTime);
        return (S) reservationTime;
    }

    @Override
    public boolean existsByStartAt(final LocalTime startAt) {
        return reservationTimes.values().stream()
                .anyMatch(reservationTime -> reservationTime.getStartAt().equals(startAt));
    }

    @Override
    public Optional<ReservationTime> findById(final Long id) {
        return Optional.ofNullable(reservationTimes.get(id));
    }

    @Override
    public void deleteById(final Long id) {
        reservationTimes.remove(id);
    }

    @Override
    public List<ReservationTime> findAll() {
        return List.copyOf(reservationTimes.values());
    }

    @Override
    public <S extends ReservationTime> Iterable<S> saveAll(Iterable<S> entities) {
        return null;
    }

    @Override
    public boolean existsById(Long aLong) {
        return false;
    }

    @Override
    public Iterable<ReservationTime> findAllById(Iterable<Long> longs) {
        return null;
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void delete(ReservationTime entity) {

    }

    @Override
    public void deleteAllById(Iterable<? extends Long> longs) {

    }

    @Override
    public void deleteAll(Iterable<? extends ReservationTime> entities) {

    }

    @Override
    public void deleteAll() {

    }
}
