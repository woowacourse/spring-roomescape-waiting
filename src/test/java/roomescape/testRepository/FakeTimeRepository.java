package roomescape.testRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import roomescape.domain.ReservationTime;
import roomescape.domain.repository.TimeRepository;
import roomescape.domain.repository.dto.TimeDataWithBookingInfo;
import roomescape.exception.NotFoundException;

public class FakeTimeRepository implements TimeRepository {

    private final List<ReservationTime> reservationTimes = new ArrayList<>();

    private Long index = 0L;

    @Override
    public Long save(ReservationTime reservationTime) {
        ReservationTime timeWithId = ReservationTime.assignId(++index, reservationTime);
        reservationTimes.add(timeWithId);
        return index;
    }

    @Override
    public List<ReservationTime> findAll() {
        return reservationTimes;
    }

    @Override
    public Optional<ReservationTime> findById(Long id) {
        return reservationTimes.stream()
                .filter(time -> time.getId().equals(id))
                .findFirst();
    }

    @Override
    public boolean deleteById(Long id) {
        ReservationTime reservationTime = findById(id)
                .orElseThrow(() -> new NotFoundException("FakeTimeRepository: 삭제하려는 id 없음"));
        reservationTimes.remove(reservationTime);
        return true;
    }

    @Override
    public List<TimeDataWithBookingInfo> getTimesWithBookingInfo(LocalDate date, Long themeId) {
        return List.of();
    }
}
