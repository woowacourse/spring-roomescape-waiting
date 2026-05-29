package roomescape.fake;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.repository.ReservationTimeQueryingDao;

public class FakeReservationTimeQueryingDao extends ReservationTimeQueryingDao {

    private final Map<Long, ReservationTime> store = new HashMap<>();

    public FakeReservationTimeQueryingDao() {
        super(null);
    }

    public void save(ReservationTime reservationTime) {
        store.put(reservationTime.getId(), reservationTime);
    }

    @Override
    public Optional<ReservationTime> findReservationTimeById(long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<ReservationTime> findAllReservationTime() {
        return List.copyOf(store.values());
    }
}
