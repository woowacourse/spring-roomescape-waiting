package roomescape.dao.fake;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.dao.ReservationTimeDao;
import roomescape.domain.ReservationTime;
import roomescape.exception.custom.NotFoundException;

public class FakeReservationTimeDao implements ReservationTimeDao {

    private final List<ReservationTime> times = new ArrayList<>();
    private final AtomicLong index = new AtomicLong(1);

    public List<ReservationTime> findAllTimes() {
        return Collections.unmodifiableList(times);
    }

    public List<ReservationTime> findAllTimesWithBooked(LocalDate date, Long themeId) {
        // 테스트 환경에선 예약 여부 확인이 어렵기 때문에 booked 값은 false로 설정
        return times.stream()
            .map(t -> new ReservationTime(t.getId(), t.getStartAt(), false))
            .toList();
    }

    public ReservationTime findTimeById(Long id) {
        return times.stream()
            .filter(t -> t.getId().equals(id))
            .findFirst()
            .orElseThrow(() -> new NotFoundException("reservationTime"));
    }

    public boolean existTimeByStartAt(LocalTime startAt) {
        return times.stream()
            .anyMatch(t -> t.getStartAt().equals(startAt));
    }

    public ReservationTime addTime(ReservationTime reservationTime) {
        ReservationTime newReservationTime = new ReservationTime(
            index.getAndIncrement(),
            reservationTime.getStartAt());

        times.add(newReservationTime);
        return newReservationTime;
    }

    public void removeTimeById(Long id) {
        times.removeIf(t -> t.getId().equals(id));
    }
}
