package roomescape.reservationtime.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.ErrorCode;
import roomescape.exception.business.BusinessException;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.domain.ReservationTimeFactory;
import roomescape.reservationtime.dto.TimeRequest;
import roomescape.reservationtime.dto.TimeResponse;
import roomescape.reservationtime.repository.ReservationTimeRepository;

@Service
@Transactional(readOnly = true)
public class ReservationTimeService {

    private final ReservationTimeRepository timeRepository;
    private final ReservationTimeFactory reservationTimeFactory;

    public ReservationTimeService(ReservationTimeRepository timeRepository, ReservationTimeFactory reservationTimeFactory) {
        this.timeRepository = timeRepository;
        this.reservationTimeFactory = reservationTimeFactory;
    }

    public ReservationTime getById(Long id) {
        return timeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.TIME_NOT_FOUND));
    }

    @Transactional
    public TimeResponse createTime(TimeRequest request) {
        ReservationTime time = reservationTimeFactory.create(request.startAt(), request.finishAt());
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
        if (timeRepository.existsReservationByTimeId(id)) {
            throw new BusinessException(ErrorCode.TIME_HAS_RESERVATION);
        }
        timeRepository.deleteById(id);
    }
}