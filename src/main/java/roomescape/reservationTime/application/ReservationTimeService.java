package roomescape.reservationTime.application;

import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservationTime.application.dto.AvailableTimeRequest;
import roomescape.reservationTime.application.dto.AvailableTimeResponse;
import roomescape.reservationTime.application.dto.TimeRequest;
import roomescape.reservationTime.application.dto.TimeResponse;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.reservationTime.domain.respository.ReservationTimeRepository;
import roomescape.reservationTime.exception.TimeAlreadyExistsException;
import roomescape.reservationTime.exception.UsingTimeException;

@Service
@AllArgsConstructor
public class ReservationTimeService {
    private final ReservationTimeRepository timeRepository;
    private final ReservationRepository reservationRepository;

    public TimeResponse create(TimeRequest request) {
        LocalTime startedAt = request.startAt();
        validateTimeNotExists(startedAt);

        ReservationTime reservationTime = new ReservationTime(request.startAt());
        return TimeResponse.from(timeRepository.save(reservationTime));
    }

    private void validateTimeNotExists(LocalTime startedAt) {
        if (timeRepository.existsByStartAt(startedAt)) {
            throw new TimeAlreadyExistsException();
        }
    }

    public List<TimeResponse> findAll() {
        return TimeResponse.from(timeRepository.findAll());
    }

    public List<AvailableTimeResponse> findAvailableTimes(AvailableTimeRequest request) {
        List<Long> bookedTimeIds = reservationRepository.findTimeIdsByDateAndTheme(request.date(), request.themeId());
        Collection<ReservationTime> times = timeRepository.findAll();

        return times.stream()
                .map(time -> {
                    boolean alreadyBooked = bookedTimeIds.contains(time.getId());
                    return AvailableTimeResponse.of(time, alreadyBooked);
                })
                .toList();
    }

    public void deleteById(Long id) {
        validateUnUsedTime(id);
        timeRepository.deleteById(id);
    }

    private void validateUnUsedTime(Long id) {
        if (reservationRepository.existsByTimeId(id)) {
            throw new UsingTimeException();
        }
    }
}
