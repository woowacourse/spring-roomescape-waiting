package roomescape.fake;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.domain.repository.AvailableReservationTime;
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
        return times.values().stream().toList();
    }

    @Override
    public List<AvailableReservationTime> findAvailableByThemeAndDate(Long themeId, LocalDate date) {
        throw new UnsupportedOperationException();
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
        return times.size() != beforeSize ? 1 : 0;
    }

    @Override
    public Boolean existsByStartAt(LocalTime startAt) {
        return times.values().stream()
                .anyMatch(t -> t.getStartAt().equals(startAt));
    }
}
