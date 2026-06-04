package roomescape.fake;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.repository.ReservationTimeQueryingDao;

public class FakeReservationTimeQueryingDao extends ReservationTimeQueryingDao {

    private final List<ReservationTime> store = new ArrayList<>();

    public FakeReservationTimeQueryingDao() {
        super(null);
    }

    public void save(ReservationTime time) {
        store.add(time);
    }

    @Override
    public Optional<ReservationTime> findReservationTimeById(long id) {
        return store.stream()
                .filter(time -> time.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<ReservationTime> findAllReservationTime() {
        return List.copyOf(store);
    }
}
