package roomescape.domain.reservationtime;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservationtime.dto.ReservationTimeAvailabilityResponse;
import roomescape.domain.reservationtime.dto.ReservationTimeResponse;
import roomescape.domain.reservationtime.dto.TimeCreationRequest;
import roomescape.domain.reservationtime.dto.TimeCreationResponse;
import roomescape.support.exception.ReservationTimeErrorCode;
import roomescape.support.exception.RoomescapeException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;

    private static boolean isAvailable(ReservationTime reservationTime, Set<Long> reservedTimeIds) {
        return !reservedTimeIds.contains(reservationTime.getId());
    }

    public TimeCreationResponse createReservationTime(TimeCreationRequest request) {
        if (reservationTimeRepository.existsByStartAt(request.startAt())) {
            throw new RoomescapeException(ReservationTimeErrorCode.RESERVATION_TIME_DUPLICATED);
        }
        ReservationTime reservationTime = reservationTimeRepository.save(request.toEntity());
        return TimeCreationResponse.from(reservationTime);
    }

    public List<ReservationTimeResponse> getAllReservationTime() {
        return reservationTimeRepository.findAll().stream()
            .map(ReservationTimeResponse::from)
            .toList();
    }

    public void deleteReservationTime(Long id) {
        if (reservationRepository.countByTimeId(id) > 0) {
            throw new RoomescapeException(ReservationTimeErrorCode.RESERVATION_TIME_IN_USE);
        }
        reservationTimeRepository.deleteById(id);
    }

    public List<ReservationTimeAvailabilityResponse> getReservationTimeAvailability(Long themeId, Long dateId) {
        List<ReservationTime> allReservationTime = reservationTimeRepository.findAll();
        Set<Long> reservedTimeIds = getReservedTimeIds(themeId, dateId);
        return allReservationTime.stream()
            .map(reservationTime -> ReservationTimeAvailabilityResponse.of(
                reservationTime,
                isAvailable(reservationTime, reservedTimeIds)
            ))
            .toList();
    }

    public ReservationTime findById(Long id) {
        return reservationTimeRepository.findById(id)
            .orElseThrow(() -> new RoomescapeException(ReservationTimeErrorCode.RESERVATION_TIME_NOT_EXIST));
    }

    private Set<Long> getReservedTimeIds(Long themeId, Long dateId) {
        List<Long> reservedTimeIds = reservationRepository.findReservedTimes(themeId, dateId);
        return new HashSet<>(reservedTimeIds);
    }
}
