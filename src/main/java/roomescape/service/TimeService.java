package roomescape.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import roomescape.controller.time.dto.AvailabilityTimeRequest;
import roomescape.controller.time.dto.AvailabilityTimeResponse;
import roomescape.controller.time.dto.CreateTimeRequest;
import roomescape.controller.time.dto.ReadTimeResponse;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Status;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.service.exception.DuplicateTimeException;
import roomescape.service.exception.TimeUsedException;

@Service
public class TimeService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository timeRepository;
    private final ReservationTimeRepository reservationTimeRepository;

    public TimeService(final ReservationRepository reservationRepository,
                       final ReservationTimeRepository timeRepository,
                       ReservationTimeRepository reservationTimeRepository) {
        this.reservationRepository = reservationRepository;
        this.timeRepository = timeRepository;
        this.reservationTimeRepository = reservationTimeRepository;
    }

    public List<ReadTimeResponse> getTimes() {
        return timeRepository.findAll().stream()
                .map(ReadTimeResponse::from)
                .toList();
    }

    public List<AvailabilityTimeResponse> getAvailabilityTimes(
            final AvailabilityTimeRequest request) {
        final LocalDate today = LocalDate.now();
        final LocalDate reservationDate = request.date();
        if (reservationDate.isBefore(today)) {
            return List.of();
        }
        final List<Reservation> reservations = reservationRepository
                .findAllJoinTimeByStatusAndDateAndThemeId(
                        Status.RESERVED, reservationDate, request.themeId());
        final Set<ReservationTime> bookedTimes = reservations.stream()
                .map(Reservation::getTime)
                .collect(Collectors.toSet());
        if (reservationDate.isEqual(today)) {
            return getAvailabilityTimesToday(bookedTimes);
        }
        return getAvailabilityTimesFuture(bookedTimes);
    }

    private List<AvailabilityTimeResponse> getAvailabilityTimesFuture(
            final Set<ReservationTime> bookedTimes) {
        return timeRepository.findAll()
                .stream()
                .map(time -> AvailabilityTimeResponse.from(time, bookedTimes.contains(time)))
                .toList();
    }

    private List<AvailabilityTimeResponse> getAvailabilityTimesToday(
            final Set<ReservationTime> bookedTimes) {
        return timeRepository.findAll()
                .stream()
                .filter(time -> time.getStartAt().isAfter(LocalTime.now()))
                .map(time -> AvailabilityTimeResponse.from(time, bookedTimes.contains(time)))
                .toList();
    }

    public AvailabilityTimeResponse addTime(final CreateTimeRequest createTimeRequest) {
        final ReservationTime time = createTimeRequest.toDomain();
        validateDuplicate(time);

        final ReservationTime savedTime = timeRepository.save(time);
        return AvailabilityTimeResponse.from(savedTime, false);
    }

    public void deleteTime(final long id) {
        if (reservationRepository.existsByTimeId(id)) {
            throw new TimeUsedException("예약된 시간은 삭제할 수 없습니다.");
        }
        final ReservationTime findTime = timeRepository.findByIdOrThrow(id);
        timeRepository.deleteById(findTime.getId());
    }

    private void validateDuplicate(final ReservationTime time) {
        final boolean hasSameTime = reservationTimeRepository.existsByStartAt(time.getStartAt());
        if (hasSameTime) {
            throw new DuplicateTimeException("중복된 시간은 생성이 불가합니다.");
        }
    }
}
