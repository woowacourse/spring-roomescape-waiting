package roomescape.application;

import java.time.LocalDate;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import roomescape.application.dto.TimeCreateDto;
import roomescape.application.dto.TimeDto;
import roomescape.domain.ReservationTime;
import roomescape.domain.repository.TimeRepository;
import roomescape.domain.repository.dto.TimeDataWithBookingInfo;
import roomescape.exception.NotFoundException;

@Service
public class TimeService {

    private final TimeRepository repository;

    public TimeService(TimeRepository repository) {
        this.repository = repository;
    }

    public List<TimeDto> getAllTimes() {
        List<ReservationTime> reservationTimes = repository.findAll();
        return TimeDto.from(reservationTimes);
    }

    public TimeDto registerNewTime(TimeCreateDto request) {
        ReservationTime newReservationTime = ReservationTime.withoutId(request.startAt());
        ReservationTime savedReservationTime = repository.save(newReservationTime);
        return TimeDto.from(savedReservationTime);
    }

    public void deleteTime(Long id) {
        try {
            repository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("예약이 존재하는 시간은 삭제할 수 없습니다.");
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("삭제하려는 id가 존재하지 않습니다. id: " + id);
        }
    }

    public TimeDto getTimeById(Long id) {
        ReservationTime reservationTime = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("찾으려는 id가 존재하지 않습니다. id: " + id));
        return TimeDto.from(reservationTime);
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
}
