package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.controller.time.dto.AvailabilityTimeRequest;
import roomescape.controller.time.dto.AvailabilityTimeResponse;
import roomescape.controller.time.dto.CreateTimeRequest;
import roomescape.controller.time.dto.ReadTimeResponse;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.service.exception.DuplicateTimeException;
import roomescape.service.exception.TimeUsedException;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TimeService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository timeRepository;

    public TimeService(final ReservationRepository reservationRepository, final ReservationTimeRepository timeRepository) {
        this.reservationRepository = reservationRepository;
        this.timeRepository = timeRepository;
    }

    public List<ReadTimeResponse> getTimes() {
        return timeRepository.findAll().stream()
                .map(ReadTimeResponse::from)
                .toList();
    }

    public List<AvailabilityTimeResponse> getTimeAvailable(final AvailabilityTimeRequest request) {
        final List<ReservationTime> times = timeRepository.findAll();
        final Set<ReservationTime> bookedTimes = reservationRepository
                .findAllByDateAndThemeId(request.date(), request.themeId())
                .stream()
                .map(Reservation::getTime)
                .collect(Collectors.toSet());

        return times.stream()
                .map(time -> AvailabilityTimeResponse.from(time, bookedTimes.contains(time)))
                .toList();
    }

    public AvailabilityTimeResponse addTime(final CreateTimeRequest createTimeRequest) {
        final ReservationTime time = createTimeRequest.toDomain();
        final List<ReservationTime> times = timeRepository.findAll();
        validateDuplicate(times, time);

        final ReservationTime savedTime = timeRepository.save(time);
        return AvailabilityTimeResponse.from(savedTime, false);
    }

    public void deleteTime(final Long id) {
        if (reservationRepository.existsByTimeId(id)) {
            throw new TimeUsedException("예약된 시간은 삭제할 수 없습니다.");
        }
        final ReservationTime findTime = timeRepository.fetchById(id);
        timeRepository.deleteById(findTime.getId());
    }

    private void validateDuplicate(final List<ReservationTime> times, final ReservationTime parsedTime) {
        final boolean hasSameTime = times.stream()
                .anyMatch(time -> time.isSameTime(parsedTime));
        if (hasSameTime) {
            throw new DuplicateTimeException("중복된 시간은 생성이 불가합니다.");
        }
    }
}
