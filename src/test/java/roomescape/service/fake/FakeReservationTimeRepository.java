package roomescape.service.fake;

import roomescape.domain.ReservationTime;
import roomescape.repository.ReservationTimeRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FakeReservationTimeRepository implements ReservationTimeRepository {

    private final List<ReservationTime> times = new ArrayList<>();

    public void add(ReservationTime time) {
        times.add(time);
    }

    @Override
    public Optional<ReservationTime> findById(long timeId) {
        return times.stream()
                .filter(t -> t.getId() == timeId)
                .findFirst();
    }

    @Override
    public List<ReservationTime> findAll() {
        return List.copyOf(times);
    }

    @Override
    public ReservationTime save(ReservationTime reservationTime) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteById(long id) {
        throw new UnsupportedOperationException();
    }
}
