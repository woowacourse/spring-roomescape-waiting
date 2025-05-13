package roomescape.fake;

import roomescape.domain.ReservationTime;
import roomescape.persistence.ReservationTimeRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class FakeReservationTimeRepository implements ReservationTimeRepository {

    private List<ReservationTime> reservationTimes = new ArrayList<>();
    private Long id = 0L;

    @Override
    public List<ReservationTime> findAll() {
        return List.copyOf(reservationTimes);
    }

    @Override
    public Long create(ReservationTime reservationTime) {
        reservationTimes.add(new ReservationTime(++id, reservationTime.startAt()));
        return id;
    }

    @Override
    public void deleteById(Long reservationTimeId) {
        reservationTimes = reservationTimes.stream()
                .filter(reservationTime -> !Objects.equals(reservationTime.id(), reservationTimeId))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ReservationTime> findById(Long reservationTimeId) {
        return reservationTimes.stream()
                .filter(reservationTime -> Objects.equals(reservationTime.id(), reservationTimeId))
                .findFirst();
    }
}
