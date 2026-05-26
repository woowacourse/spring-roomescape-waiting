package roomescape.domain.reservationtime;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.domain.reservation.ReservationSlotRepository;
import roomescape.domain.reservationdate.ReservationDateRepository;
import roomescape.domain.reservationtime.admin.dto.CreateTimeRequest;
import roomescape.domain.reservationtime.admin.dto.CreateTimeResponse;
import roomescape.domain.reservationtime.admin.dto.ReservationTimeResponse;
import roomescape.domain.reservationtime.dto.ReservationTimeAvailabilityResponse;
import roomescape.domain.theme.ThemeRepository;
import roomescape.support.exception.ConflictException;
import roomescape.support.exception.NotFoundException;
import roomescape.support.exception.errors.ReservationDateErrors;
import roomescape.support.exception.errors.ReservationTimeErrors;
import roomescape.support.exception.errors.ThemeErrors;

@Service
@RequiredArgsConstructor
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationSlotRepository reservationRepository;
    private final ThemeRepository themeRepository;
    private final ReservationDateRepository reservationDateRepository;

    public CreateTimeResponse createReservationTime(CreateTimeRequest request) {
        ReservationTime reservationTime = reservationTimeRepository.save(request.toEntity());
        return CreateTimeResponse.from(reservationTime);
    }

    public List<ReservationTimeResponse> getAllReservationTime() {
        return reservationTimeRepository.findAll().stream()
            .map(ReservationTimeResponse::from)
            .toList();
    }

    public void deleteReservationTime(Long id) {
        if (reservationRepository.countByTimeId(id) > 0) {
            throw new ConflictException(ReservationTimeErrors.RESERVATION_TIME_IN_USE);
        }
        reservationTimeRepository.deleteById(id);
    }

    public List<ReservationTimeAvailabilityResponse> getReservationTimeAvailability(Long themeId, Long dateId) {
        validateThemeAndDateExists(themeId, dateId);
        List<ReservationTime> allReservationTime = reservationTimeRepository.findAll();
        Set<Long> reservedTimeIds = getReservedTimeIds(themeId, dateId);
        return allReservationTime.stream()
            .map(reservationTime -> ReservationTimeAvailabilityResponse.of(
                reservationTime,
                isAvailable(reservationTime, reservedTimeIds)
            ))
            .toList();
    }

    private void validateThemeAndDateExists(Long themeId, Long dateId) {
        themeRepository.findById(themeId)
            .orElseThrow(() -> new NotFoundException(ThemeErrors.THEME_NOT_EXIST));
        reservationDateRepository.findById(dateId)
            .orElseThrow(() -> new NotFoundException(ReservationDateErrors.RESERVATION_DATE_NOT_EXIST));
    }

    private Set<Long> getReservedTimeIds(Long themeId, Long dateId) {
        List<Long> reservedTimeIds = reservationRepository.findReservedTimes(themeId, dateId);
        return new HashSet<>(reservedTimeIds);
    }

    private static boolean isAvailable(ReservationTime reservationTime, Set<Long> reservedTimeIds) {
        return !reservedTimeIds.contains(reservationTime.getId());
    }
}
