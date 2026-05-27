package roomescape.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.ReservationTime;
import roomescape.exception.CustomInvalidRequestException;
import roomescape.exception.ErrorCode;
import roomescape.repository.ReservationTimeRepository;
import roomescape.service.dto.request.ServiceReservationTimeCreateRequest;
import roomescape.service.dto.response.ServiceReservationTimeAvailabilityResponse;
import roomescape.service.dto.response.ServiceReservationTimeResponse;

@Component
@Transactional(readOnly = true)
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final Clock clock;

    public ReservationTimeService(ReservationTimeRepository reservationTimeRepository, Clock clock) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.clock = clock;
    }

    @Transactional
    public ServiceReservationTimeResponse save(ServiceReservationTimeCreateRequest request) {
        validateDuplicatedReservationTime(request.startAt());

        ReservationTime reservationTime = reservationTimeRepository.save(request.toEntity());
        return ServiceReservationTimeResponse.from(reservationTime);
    }

    public List<ServiceReservationTimeResponse> findAll() {
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
        return reservationTimes.stream()
                .map(ServiceReservationTimeResponse::from)
                .toList();
    }

    private void validateDuplicatedReservationTime(LocalTime startAt) {
        if (reservationTimeRepository.existsByStartAt(startAt)) {
            throw new CustomInvalidRequestException(ErrorCode.DUPLICATED_RESERVATION_TIME);
        }
    }

    public List<ServiceReservationTimeAvailabilityResponse> findAvailabilityByDateAndTheme(
            LocalDate date, Long themeId) {
        validateNotPastDate(date);

        List<ReservationTime> allReservationTimes = reservationTimeRepository.findAll();
        List<Long> reservedTimeIdByDateAndTheme = reservationTimeRepository.findReservedTimeIdByDateAndTheme(date,
                themeId);

        return allReservationTimes.stream()
                .map(reservationTime -> {
                    if (reservedTimeIdByDateAndTheme.contains(reservationTime.getId())) {
                        return ServiceReservationTimeAvailabilityResponse.from(reservationTime, false);
                    }
                    return ServiceReservationTimeAvailabilityResponse.from(reservationTime, true);
                }).toList();
    }

    private void validateNotPastDate(LocalDate date) {
        if (date.isBefore(LocalDate.now(clock))) {
            throw new CustomInvalidRequestException(ErrorCode.PAST_RESERVATION_TIME_READ);
        }
    }

    @Transactional
    public void delete(Long id) {
        reservationTimeRepository.delete(id);
    }

    public ReservationTime findReservationTime(Long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new CustomInvalidRequestException(ErrorCode.NOT_FOUND_RESERVATION_TIME));
    }
}
