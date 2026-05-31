package roomescape.support.fake;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationtime.ReservationTimeRepository;

public class FakeReservationTimeRepository implements ReservationTimeRepository {

    private final Map<Long, ReservationTime> storage = new LinkedHashMap<>();
    private long sequence = 1L;

    @Override
    public Optional<ReservationTime> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public ReservationTime save(ReservationTime reservationTime) {
        Long id = reservationTime.getId();
        if (id == null) {
            id = sequence++;
        } else {
            sequence = Math.max(sequence, id + 1);
        }
        ReservationTime savedReservationTime = ReservationTime.of(id, reservationTime.getStartAt());
        storage.put(id, savedReservationTime);
        return savedReservationTime;
    }

    @Override
    public List<ReservationTime> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public int deleteById(Long id) {
        ReservationTime removedReservationTime = storage.remove(id);
        if (removedReservationTime == null) {
            return 0;
        }
        return 1;
    }

    @Override
    public Optional<ReservationTime> findByStartAt(LocalTime startAt) {
        return storage.values().stream()
            .filter(reservationTime -> startAt.equals(reservationTime.getStartAt()))
            .findFirst();
    }
}
