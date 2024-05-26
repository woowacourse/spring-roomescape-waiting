package roomescape.application;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.dto.AvailableTimeResponse;
import roomescape.application.dto.TimeRequest;
import roomescape.application.dto.TimeResponse;
import roomescape.domain.Time;
import roomescape.domain.repository.ReservationQueryRepository;
import roomescape.domain.repository.TimeCommandRepository;
import roomescape.domain.repository.TimeQueryRepository;
import roomescape.exception.RoomescapeErrorCode;
import roomescape.exception.RoomescapeException;

@Service
@Transactional(readOnly = true)
public class TimeService {

    private final TimeCommandRepository timeCommandRepository;
    private final TimeQueryRepository timeQueryRepository;
    private final ReservationQueryRepository reservationQueryRepository;

    public TimeService(TimeCommandRepository timeCommandRepository,
                       ReservationQueryRepository reservationQueryRepository,
                       TimeQueryRepository timeQueryRepository) {
        this.timeCommandRepository = timeCommandRepository;
        this.reservationQueryRepository = reservationQueryRepository;
        this.timeQueryRepository = timeQueryRepository;
    }

    @Transactional
    public TimeResponse create(TimeRequest request) {
        LocalTime startAt = request.startAt();
        if (existsByStartAt(startAt)) {
            throw new RoomescapeException(RoomescapeErrorCode.DUPLICATED_TIME,
                    String.format("중복된 예약 시간입니다. 요청 예약 시간:%s", startAt));
        }

        Time time = timeCommandRepository.save(request.toReservationTime());
        return TimeResponse.from(time);
    }

    @Transactional
    public void deleteById(Long id) {
        Time time = timeQueryRepository.getById(id);
        if (reservationQueryRepository.existsByTime(time)) {
            throw new RoomescapeException(RoomescapeErrorCode.ALREADY_RESERVED,
                    String.format("해당 예약 시간에 연관된 예약이 존재하여 삭제할 수 없습니다. 삭제 요청한 시간:%s", time.getStartAt()));
        }
        timeCommandRepository.delete(time);
    }

    public List<TimeResponse> findAll() {
        return timeQueryRepository.findAll()
                .stream()
                .map(TimeResponse::from)
                .toList();
    }

    public List<AvailableTimeResponse> findAvailableTimes(LocalDate date, Long themeId) {
        return reservationQueryRepository.findAvailableReservationTimes(date, themeId)
                .stream()
                .map(AvailableTimeResponse::from)
                .toList();
    }

    private boolean existsByStartAt(LocalTime startAt) {
        return timeQueryRepository.existsByStartAt(startAt);
    }
}
