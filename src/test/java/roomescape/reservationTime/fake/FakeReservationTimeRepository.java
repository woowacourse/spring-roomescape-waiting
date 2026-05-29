package roomescape.reservationTime.fake;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.reservationTime.domain.ReservationTimeRepository;

public class FakeReservationTimeRepository implements ReservationTimeRepository {

    private final Map<Long, ReservationTime> store = new HashMap<>();
    private Long sequence = 0L;

    @Override
    public ReservationTime save(ReservationTime reservationTime) {
        if (reservationTime.getId() == null) {
            ReservationTime saved = ReservationTime.createRow(
                    sequence++,
                    reservationTime.getStartAt()
            );
            store.put(saved.getId(), saved);
            return saved;
        }
        store.put(reservationTime.getId(), reservationTime);
        return reservationTime;
    }

    @Override
    public Optional<ReservationTime> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<ReservationTime> findAll() {
        return store.values().stream().toList();
    }

    @Override
    public void deleteById(Long id) {
        store.remove(id);
    }
}
