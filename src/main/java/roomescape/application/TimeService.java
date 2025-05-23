package roomescape.application;

import java.time.LocalDate;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.dto.TimeCreateServiceRequest;
import roomescape.application.dto.TimeServiceResponse;
import roomescape.domain.entity.ReservationTime;
import roomescape.domain.repository.TimeRepository;
import roomescape.domain.repository.dto.TimeDataWithBookingInfo;
import roomescape.exception.NotFoundException;

@Service
@Transactional(readOnly = true)
public class TimeService {

    private final TimeRepository repository;

    public TimeService(TimeRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public TimeServiceResponse registerNewTime(TimeCreateServiceRequest request) {
        ReservationTime newReservationTime = ReservationTime.withoutId(request.startAt());
        ReservationTime savedReservationTime = repository.save(newReservationTime);
        return TimeServiceResponse.from(savedReservationTime);
    }

    public List<TimeServiceResponse> getAllTimes() {
        List<ReservationTime> reservationTimes = repository.findAll();
        return TimeServiceResponse.from(reservationTimes);
    }

    public TimeServiceResponse getTimeById(Long id) {
        ReservationTime reservationTime = getTimeEntityById(id);
        return TimeServiceResponse.from(reservationTime);
    }

    public ReservationTime getTimeEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("찾으려는 id가 존재하지 않습니다. id: " + id));
    }

    public List<TimeDataWithBookingInfo> getTimesWithBookingInfo(LocalDate date, Long themeId) {
        List<ReservationTime> times = repository.findAll();
        return times.stream()
                .map(time ->
                        new TimeDataWithBookingInfo(
                                time.getId(),
                                time.getStartAt(),
                                time.hasReservationOn(date, themeId)
                        )
                )
                .toList();
    }

    @Transactional
    public void deleteTime(Long id) {
        try {
            repository.deleteById(id);
            repository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("예약이 존재하는 시간은 삭제할 수 없습니다.");
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("삭제하려는 id가 존재하지 않습니다. id: " + id);
        }
    }
}
