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
    public List<ReservationTime> findAll() {
        return times;
    }

    @Override
    public Optional<ReservationTime> findById(final long id) {
        return times.stream()
                .filter(time -> time.getId() == id)
                .findFirst();
    }

    @Override
    public void deleteById(final long id) {
        ReservationTime reservationTime = findById(id).orElseThrow();
        times.remove(reservationTime);
    }

    @Override
    public boolean existsByTime(final LocalTime reservationTime) {
        return times.stream()
                .anyMatch(time -> time.getStartAt().equals(reservationTime));
    }
}
