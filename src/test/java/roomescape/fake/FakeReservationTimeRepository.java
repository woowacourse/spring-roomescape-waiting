package roomescape.fake;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.domain.reservationtime.AvailableReservationTime;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationtime.ReservationTimeRepository;

public class FakeReservationTimeRepository implements ReservationTimeRepository {

    private final List<ReservationTime> store = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

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

    @Override
    public List<AvailableReservationTime> findAvailableReservationTime(LocalDate date, Long themeId) {
        return store.stream()
                .map(time -> new AvailableReservationTime(time.getId(), time.getStartAt(), true))
                .toList();
    }

    @Override
    public Long insert(ReservationTime reservationTime) {
        long id = idGenerator.getAndIncrement();
        store.add(new ReservationTime(id, reservationTime.getStartAt()));
        return id;
    }

    @Override
    public void save(Long id, LocalTime startAt) {
        for (int i = 0; i < store.size(); i++) {
            if (store.get(i).getId().equals(id)) {
                store.set(i, new ReservationTime(id, startAt));
                return;
            }
        }
    }

    @Override
    public void delete(Long id) {
        store.removeIf(time -> time.getId().equals(id));
    }
}
