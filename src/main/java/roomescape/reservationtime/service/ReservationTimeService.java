package roomescape.reservationtime.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.BusinessException;
import roomescape.common.exception.ErrorCode;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.dto.TimeRequest;
import roomescape.reservationtime.dto.TimeResponse;
import roomescape.reservationtime.repository.ReservationTimeRepository;

@Service
public class ReservationTimeService {

    private final ReservationTimeRepository timeRepository;

    public ReservationTimeService(ReservationTimeRepository timeRepository) {
        this.timeRepository = timeRepository;
    }

    public ReservationTime getById(Long id) {
        return timeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.TIME_NOT_FOUND));
    }

    @Transactional
    public TimeResponse createTime(TimeRequest request) {
        ReservationTime time = ReservationTime.of(request.startAt(), request.finishAt());
        ReservationTime saved = timeRepository.save(time);
        return TimeResponse.from(saved);
    }

    public List<TimeResponse> getAllTimes() {
        return timeRepository.findAll().stream()
                .map(TimeResponse::from)
                .collect(Collectors.toList());
    }

    public List<TimeResponse> getAvailableTimes(LocalDate date, Long themeId) {
        return timeRepository.findAvailableByDateAndThemeId(date, themeId).stream()
                .map(TimeResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteById(Long id) {
        if (timeRepository.hasReservation(id)) {
            throw new BusinessException(ErrorCode.TIME_HAS_RESERVATION);
        }
        timeRepository.deleteById(id);
    }
}