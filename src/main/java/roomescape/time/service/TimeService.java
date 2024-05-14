package roomescape.time.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.time.dao.TimeDao;
import roomescape.time.domain.ReservationTime;
import roomescape.time.dto.AvailableTimeResponse;
import roomescape.time.dto.TimeCreateRequest;
import roomescape.time.dto.TimeResponse;

@Service
public class TimeService {
    private final TimeDao timeDao;

    public TimeService(TimeDao timeDao) {
        this.timeDao = timeDao;
    }

    public List<TimeResponse> findTimes() {
        return timeDao.findTimes()
                .stream()
                .map(TimeResponse::from)
                .toList();
    }

    public List<AvailableTimeResponse> findAvailableTimes(LocalDate date, Long themeId) {
        List<ReservationTime> allTime = timeDao.findTimes();
        List<ReservationTime> alreadyBookedTime = timeDao.findTimesExistsReservationDateAndThemeId(date, themeId);

        return allTime.stream()
                .map(time -> AvailableTimeResponse.of(time, alreadyBookedTime.contains(time)))
                .toList();
    }

    public TimeResponse createTime(TimeCreateRequest request) {
        ReservationTime createdTime = timeDao.createTime(request.createReservationTime());
        return TimeResponse.from(createdTime);
    }

    public void deleteTime(Long id) {
        timeDao.deleteTime(id);
    }
}
