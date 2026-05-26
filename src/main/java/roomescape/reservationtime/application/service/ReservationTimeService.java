package roomescape.reservationtime.application.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.RoomEscapeException;
import roomescape.reservation.application.service.ReservationQueryService;
import roomescape.reservationtime.application.dto.AvailableReservationTimeQueryResult;
import roomescape.reservationtime.application.dto.ReservationTimeCreateCommand;
import roomescape.reservationtime.application.dto.ReservationTimeQueryResult;
import roomescape.reservationtime.application.exception.ReservationTimeErrorCode;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.domain.repository.AvailableReservationTime;
import roomescape.reservationtime.domain.repository.AvailableReservationTimeRepository;
import roomescape.reservationtime.domain.repository.ReservationTimeRepository;

@RequiredArgsConstructor
@Transactional
@Service
public class ReservationTimeService {

    private final ReservationTimeRepository timeRepository;
    private final AvailableReservationTimeRepository availableTimeRepository;
    private final ReservationQueryService queryService;

    @Transactional(readOnly = true)
    public ReservationTimeQueryResult findById(Long timeId) {
        return ReservationTimeQueryResult.from(timeRepository.findById(timeId)
                .orElseThrow(() -> new RoomEscapeException(ReservationTimeErrorCode.TIME_NOT_FOUND)));
    }

    @Transactional(readOnly = true)
    public List<ReservationTimeQueryResult> findAll() {
        List<ReservationTime> times = timeRepository.findAll();

        return times.stream()
                .map(ReservationTimeQueryResult::from)
                .toList();
    }

    public List<AvailableReservationTimeQueryResult> findAvailableTimes(Long themeId, LocalDate date) {
        List<AvailableReservationTime> times = availableTimeRepository.findByThemeAndDate(themeId, date);

        return times.stream()
                .map(AvailableReservationTimeQueryResult::from)
                .toList();
    }

    public ReservationTimeQueryResult save(ReservationTimeCreateCommand request) {
        validateDuplicateTime(request.startAt());
        ReservationTime time = request.toEntity();

        ReservationTime savedTime = timeRepository.save(time);

        return ReservationTimeQueryResult.from(savedTime);
    }

    public int delete(Long id) {
        validateNoReservationExists(id);
        return timeRepository.delete(id);
    }

    private void validateNoReservationExists(Long id) {
        if (queryService.existsByTimeId(id)) {
            throw new RoomEscapeException(ReservationTimeErrorCode.TIME_DELETE_NOT_ALLOWED);
        }
    }

    private void validateDuplicateTime(LocalTime startAt) {
        if (timeRepository.existsByStartAt(startAt)) {
            throw new RoomEscapeException(
                    ReservationTimeErrorCode.DUPLICATE_TIME,
                    startAt.format(DateTimeFormatter.ofPattern("HH:mm"))
            );
        }
    }
}
