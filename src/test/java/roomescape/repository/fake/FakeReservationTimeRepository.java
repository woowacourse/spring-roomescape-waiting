package roomescape.repository.fake;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.repository.ReservationTimeRepository;
import roomescape.entity.ReservationTime;
import roomescape.exception.custom.NotFoundException;

public class FakeReservationTimeRepository implements ReservationTimeRepository {

    private final List<ReservationTime> times = new ArrayList<>();

    public List<ReservationTime> findAll() {
        return Collections.unmodifiableList(times);
    }

    public List<ReservationTime> findAllTimesWithBooked(LocalDate date, Long themeId) {
        // 테스트 환경에선 예약 여부 확인이 어렵기 때문에 booked 값은 false로 설정
        return times.stream()
            .map(t -> new ReservationTime(t.getStartAt()))
            .toList();
    }

    public Optional<ReservationTime> findById(Long id) {
        return times.stream()
            .filter(t -> t.getId().equals(id))
            .findFirst();
    }

    public boolean existsByStartAt(LocalTime startAt) {
        return times.stream()
            .anyMatch(t -> t.getStartAt().equals(startAt));
    }

    public ReservationTime save(ReservationTime reservationTime) {
        ReservationTime newReservationTime = new ReservationTime(
            reservationTime.getStartAt());

        times.add(newReservationTime);
        return newReservationTime;
    }

    public void deleteById(Long id) {
        times.removeIf(t -> t.getId().equals(id));
    }
}
