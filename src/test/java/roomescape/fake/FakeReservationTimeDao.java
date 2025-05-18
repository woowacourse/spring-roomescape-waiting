package roomescape.fake;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import roomescape.time.domain.ReservationAvailability;
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
    public List<ReservationTime> findAll() {
        return times;
    }

    @Override
    public Optional<ReservationTime> findById(final Long aLong) {
        return times.stream()
                .filter(time -> time.getId() == aLong)
                .findFirst();
    }

    @Override
    public void deleteById(final Long id) {
        ReservationTime reservationTime = findById(id).orElseThrow();
        times.remove(reservationTime);
    }

    @Override
    public boolean existsByStartAt(final LocalTime reservationTime) {
        return times.stream()
                .anyMatch(time -> time.getStartAt().equals(reservationTime));
    }

    @Override
    public <S extends ReservationTime> Iterable<S> saveAll(final Iterable<S> entities) {
        throw new IllegalStateException("사용하지 않는 메서드입니다.");
    }

    @Override
    public boolean existsById(final Long aLong) {
        throw new IllegalStateException("사용하지 않는 메서드입니다.");
    }

    @Override
    public Iterable<ReservationTime> findAllById(final Iterable<Long> longs) {
        throw new IllegalStateException("사용하지 않는 메서드입니다.");
    }

    @Override
    public long count() {
        throw new IllegalStateException("사용하지 않는 메서드입니다.");
    }

    @Override
    public void delete(final ReservationTime entity) {
        throw new IllegalStateException("사용하지 않는 메서드입니다.");
    }

    @Override
    public void deleteAllById(final Iterable<? extends Long> longs) {
        throw new IllegalStateException("사용하지 않는 메서드입니다.");
    }

    @Override
    public void deleteAll(final Iterable<? extends ReservationTime> entities) {
        throw new IllegalStateException("사용하지 않는 메서드입니다.");
    }

    @Override
    public void deleteAll() {
        throw new IllegalStateException("사용하지 않는 메서드입니다.");
    }

    @Override
    public List<ReservationAvailability> findAllReservationAvailability(final LocalDate date, final long themeId) {
        throw new IllegalStateException("사용하지 않는 메서드입니다.");
    }
}
