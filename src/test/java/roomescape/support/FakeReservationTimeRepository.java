package roomescape.support;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.repository.reservationtime.ReservationTimeRepository;

public class FakeReservationTimeRepository implements ReservationTimeRepository {

    private final Map<Long, ReservationTime> reservationTimes = new LinkedHashMap<>();
    private long sequence = 1L;

    @Override
    public List<ReservationTime> findAll() {
        return new ArrayList<>(reservationTimes.values());
    }

    @Override
    public Optional<ReservationTime> findById(final long timeId) {
        return Optional.ofNullable(reservationTimes.get(timeId));
    }

    @Override
    public int deleteById(final long timeId) {
        if (reservationTimes.remove(timeId) == null) {
            return 0;
        }

        return 1;
    }

    @Override
    public ReservationTime save(final ReservationTime reservationTime) {
        ReservationTime saved = reservationTime;
        if (saved.getId() == null) {
            saved = ReservationTime.of(sequence++, reservationTime.getStartAt());
        }

        reservationTimes.put(saved.getId(), saved);
        return saved;
    }

    @Override
    public boolean existsByStartAt(final LocalTime startAt) {
        return reservationTimes.values()
                .stream()
                .anyMatch(reservationTime -> reservationTime.getStartAt().equals(startAt));
    }
}
