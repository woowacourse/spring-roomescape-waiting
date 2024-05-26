package roomescape.time.service;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import roomescape.global.exception.model.RoomEscapeException;
import roomescape.time.domain.Time;
import roomescape.time.dto.TimeRequest;
import roomescape.time.exception.TimeExceptionCode;
import roomescape.time.exception.model.TimeNotFoundException;
import roomescape.time.repository.TimeRepository;

@Service
public class TimeService {

    private final TimeRepository timeRepository;

    public TimeService(TimeRepository timeRepository) {
        this.timeRepository = timeRepository;
    }

    public Time addReservationTime(TimeRequest timeRequest) {
        validateDuplicateTime(timeRequest.startAt());

        Time time = Time.from(timeRequest.startAt());
        return timeRepository.save(time);
    }

    public List<Time> findReservationTimes() {
        return timeRepository.findAllByOrderByStartAt();
    }

    public Time findTime(long timeId) {
        return timeRepository.findById(timeId)
                .orElseThrow(TimeNotFoundException::new);
    }

    public List<Time> findTimesOrderByStartAt() {
        return timeRepository.findAllByOrderByStartAt();
    }

    public void removeReservationTime(long reservationTimeId) {
        timeRepository.deleteById(reservationTimeId);
    }

    public void validateDuplicateTime(LocalTime startAt) {
        Optional<Time> time = timeRepository.findByStartAt(startAt);

        if (time.isPresent()) {
            throw new RoomEscapeException(TimeExceptionCode.DUPLICATE_TIME_EXCEPTION);
        }
    }
}
