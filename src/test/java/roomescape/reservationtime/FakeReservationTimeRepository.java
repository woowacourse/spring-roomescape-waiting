package roomescape.reservationtime;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.dao.EmptyResultDataAccessException;

public class FakeReservationTimeRepository implements ReservationTimeRepository {

    private final List<ReservationTime> reservationTimes = new ArrayList<>();
    private final List<Long> invokeDeleteId = new ArrayList<>();
    private Long NEXT_ID = 1L;

    @Override
    public Long save(final ReservationTime reservationTime) {
        final ReservationTime saveReservationTime = new ReservationTime(NEXT_ID++, reservationTime.getStartAt());
        reservationTimes.add(saveReservationTime);
        return saveReservationTime.getId();
    }

    @Override
    public ReservationTime findById(final Long id) {
        return reservationTimes.stream()
                .filter(time -> Objects.equals(time.getId(), id))
                .findAny()
                .orElseThrow(() -> new EmptyResultDataAccessException(1));
    }

    @Override
    public List<ReservationTime> findAll() {
        return new ArrayList<>(reservationTimes);
    }

    @Override
    public void deleteById(final Long id) {
        reservationTimes.stream()
                .filter(time -> Objects.equals(time.getId(), id))
                .findAny()
                .ifPresent(time -> reservationTimes.remove(time));

        invokeDeleteId.add(id);
    }

    @Override
    public Boolean existsById(final Long id) {
        return reservationTimes.stream()
                .anyMatch(reservationTime -> Objects.equals(reservationTime.getId(), id));
    }

    @Override
    public Boolean existsByStartAt(final LocalTime startAt) {
        return reservationTimes.stream()
                .anyMatch(reservationTime -> Objects.equals(reservationTime.getStartAt(), startAt));
    }

    public boolean isInvokeDeleteId(final Long id) {
        return invokeDeleteId.stream()
                .anyMatch(timeId -> Objects.equals(timeId, id));
    }

    public void clear() {
        this.NEXT_ID = 1L;
        this.reservationTimes.clear();
        this.invokeDeleteId.clear();
    }
}
