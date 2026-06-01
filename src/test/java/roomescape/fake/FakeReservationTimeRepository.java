package roomescape.fake;

import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.domain.repository.ReservationTimeRepository;

public class FakeReservationTimeRepository implements ReservationTimeRepository {

    private final Map<Long, ReservationTime> times = new LinkedHashMap<>();
    private Long idHolder = 1L;

    @Override
    public Optional<ReservationTime> findById(Long id) {
        return Optional.ofNullable(times.get(id));
    }

    @Override
    public List<ReservationTime> findAll() {
        return times.values().stream()
                .toList();
    }

    @Override
    public ReservationTime save(ReservationTime time) {
        ReservationTime savedTime = time.withId(idHolder);
        times.put(idHolder++, savedTime);
        return savedTime;
    }

    @Override
    public Integer delete(Long id) {
        int beforeSize = times.size();
        times.remove(id);
        int afterSize = times.size();

        if (beforeSize != afterSize) {
            return 1;
        }

        return 0;
    }

    @Override
    public Boolean existsByStartAt(LocalTime startAt) {
        return times.values().stream()
                .anyMatch(savedTimes -> savedTimes.getStartAt().equals(startAt));
    }
}
