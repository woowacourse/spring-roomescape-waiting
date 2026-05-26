package roomescape.domain.reservationtime;

import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;
import roomescape.domain.reservationtime.dto.TimeRequest;
import roomescape.domain.reservationtime.dto.TimeResponse;

@Service
public class ReservationTimeService {

    private final ReservationTimeRepository timeRepository;

    public ReservationTimeService(ReservationTimeRepository timeRepository) {
        this.timeRepository = timeRepository;
    }

    public TimeResponse createTime(TimeRequest request) {
        validateDuplicateTime(request.startAt());
        ReservationTime time = ReservationTime.of(
            request.startAt(),
            request.finishAt()
        );
        ReservationTime saved = timeRepository.save(time);
        return TimeResponse.from(saved);
    }

    public List<TimeResponse> getAllTimes() {
        List<ReservationTime> times = timeRepository.findAll();

        return times.stream()
            .map(TimeResponse::from)
            .toList();
    }

    public void deleteById(Long id) {
        validateReservationTimeId(id);
        timeRepository.deleteById(id);
    }

    private void validateDuplicateTime(LocalTime newTime) {
        if (timeRepository.existsByStartAt(newTime)) {
            throw new RoomescapeException(ErrorCode.DUPLICATE_TIME);
        }
    }

    private void validateReservationTimeId(Long id) {
        if (!timeRepository.existsById(id)) {
            throw new RoomescapeException(ErrorCode.TIME_ID_NOT_FOUND);
        }
    }
}
