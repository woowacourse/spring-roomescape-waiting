package roomescape.reservation.unit.repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.reservation.dto.response.ReservationTimeResponse.AvailableReservationTimeResponse;
import roomescape.reservation.entity.ReservationTime;
import roomescape.reservation.repository.ReservationTimeRepository;

public class FakeReservationTimeRepository implements ReservationTimeRepository {

    private final Map<Long, ReservationTime> times = new ConcurrentHashMap<>();
    private final AtomicLong index = new AtomicLong(1);

    @Override
    public ReservationTime save(ReservationTime time) {
        Long id = index.getAndIncrement();
        ReservationTime savedTime = new ReservationTime(id, time.getStartAt());
        times.put(id, savedTime);
        return savedTime;
    }

    @Override
    public List<ReservationTime> findAll() {
        return new ArrayList<>(times.values());
    }

    @Override
    public List<AvailableReservationTimeResponse> findAvailableTimes(LocalDate date, Long themeId) {
        return times.values().stream()
                .map(time -> new AvailableReservationTimeResponse(
                        time.getId(),
                        time.getStartAt().toString(),
                        false
                ))
                .toList();
    }

    @Override
    public Optional<ReservationTime> findById(Long id) {
        return Optional.ofNullable(times.get(id));
    }

    @Override
    public boolean deleteById(Long id) {
        return times.remove(id) != null;
    }
}
