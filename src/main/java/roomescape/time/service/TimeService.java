package roomescape.time.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.time.domain.ReservationTime;
import roomescape.time.dto.AvailableTimeResponse;
import roomescape.time.dto.TimeCreateRequest;
import roomescape.time.dto.TimeResponse;
import roomescape.time.repository.TimeRepository;

@Service
public class TimeService {
    private final TimeRepository timeRepository;

    public TimeService(TimeRepository timeRepository) {
        this.timeRepository = timeRepository;
    }

    public List<TimeResponse> findTimes() {
        return timeRepository.findAll()
                .stream()
                .map(TimeResponse::from)
                .toList();
    }

    public List<AvailableTimeResponse> findAvailableTimes(LocalDate date, Long themeId) {
        List<ReservationTime> allTime = timeRepository.findAll();
        List<ReservationTime> alreadyBookedTime = timeRepository.findTimesExistsReservationDateAndThemeId(date, themeId);

        return allTime.stream()
                .map(time -> AvailableTimeResponse.of(time, alreadyBookedTime.contains(time)))
                .toList();
    }

    public TimeResponse createTime(TimeCreateRequest request) {
        ReservationTime time = request.createReservationTime();
        validateExists(time);
        return TimeResponse.from(timeRepository.save(time));
    }

    private void validateExists(ReservationTime time) {
        if (timeRepository.existsByStartAt(time.getStartAt())) {
            throw new IllegalArgumentException("예약 시간은 중복될 수 없습니다.");
        }
    }

    public void deleteTime(Long id) {
        timeRepository.deleteById(id);
    }
}
