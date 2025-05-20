package roomescape.unit.fake;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.infrastructure.ReservationRepository;
import roomescape.reservation.infrastructure.ReservationTimeRepository;

public class FakeReservationTimeRepository implements ReservationTimeRepository {

    private final List<ReservationTime> reservationTimes = new ArrayList<>();
    private final AtomicLong index = new AtomicLong(1);
    private final ReservationRepository reservationRepository;

    public FakeReservationTimeRepository(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Override
    public List<ReservationTime> findAll() {
        return new ArrayList<>(reservationTimes);
    }

    @Override
    public ReservationTime save(ReservationTime reservationTime) {
        ReservationTime reservationTimeWithId = new ReservationTime(index.getAndIncrement(),
                reservationTime.getStartAt());
        reservationTimes.add(reservationTimeWithId);
        return reservationTimeWithId;
    }

    @Override
    public void deleteById(Long id) {
        reservationTimes.removeIf(reservationTime -> reservationTime.getId().equals(id));
    }

    @Override
    public Optional<ReservationTime> findById(Long id) {
        return reservationTimes.stream()
                .filter(reservationTime -> reservationTime.getId().equals(id))
                .findFirst();
    }
}
