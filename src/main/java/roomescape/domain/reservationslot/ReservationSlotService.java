package roomescape.domain.reservationslot;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import roomescape.domain.reservation.JpaReservationRepository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservation.dto.ReservationCountResult;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationdate.JpaReservationDateRepository;
import roomescape.domain.reservationslot.dto.ReservationSlotResponse;
import roomescape.domain.reservationtime.JpaReservationTimeRepository;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.JpaThemeRepository;
import roomescape.support.exception.NotFoundException;
import roomescape.support.exception.errors.ReservationDateErrors;
import roomescape.support.exception.errors.ReservationSlotErrors;
import roomescape.support.exception.errors.ThemeErrors;

@Service
@RequiredArgsConstructor
public class ReservationSlotService {

    private final JpaReservationSlotRepository reservationSlotRepository;
    private final JpaThemeRepository themeRepository;
    private final JpaReservationDateRepository reservationDateRepository;
    private final JpaReservationRepository reservationRepository;
    private final JpaReservationTimeRepository reservationTimeRepository;

    public List<ReservationSlotResponse> getReservationSlots(Long themeId, Long dateId) {
        validateThemeAndDateExists(themeId, dateId);
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAllByOrderByStartAtAsc();
        List<Reservation> reservations = reservationRepository.findReservationsForSlotAvailability(
            themeId,
            dateId,
            ReservationStatus.CANCELED
        );

        return createReservationCountResults(reservationTimes, reservations).stream()
            .map(ReservationSlotResponse::from)
            .toList();
    }

    private List<ReservationCountResult> createReservationCountResults(
        List<ReservationTime> reservationTimes,
        List<Reservation> reservations
    ) {
        Map<Long, Long> reservationCountByTimeId = reservations.stream()
            .collect(Collectors.groupingBy(
                reservation -> reservation.getReservationSlot().getTime().getId(),
                Collectors.counting()
            ));

        return reservationTimes.stream()
            .map(reservationTime -> ReservationCountResult.of(
                reservationTime.getId(),
                reservationTime.getStartAt(),
                reservationCountByTimeId.getOrDefault(reservationTime.getId(), 0L)
            ))
            .toList();
    }

    public ReservationSlot findOrCreateReservationSlot(
        ReservationDate reservationDate,
        ReservationTime reservationTime,
        Theme theme
    ) {
        try {
            return reservationSlotRepository.findSlotForCreation(
                reservationTime.getId(),
                reservationDate.getId(),
                theme.getId()
            ).orElseGet(() -> saveReservationSlot(reservationDate, reservationTime, theme));
        } catch (DataIntegrityViolationException e) {
            return reservationSlotRepository.findSlotForCreation(
                reservationTime.getId(),
                reservationDate.getId(),
                theme.getId()
            ).orElseThrow(() -> new NotFoundException(ReservationSlotErrors.RESERVATION_SLOT_NOT_FOUND));
        }
    }

    private ReservationSlot saveReservationSlot(
        ReservationDate reservationDate,
        ReservationTime reservationTime,
        Theme theme
    ) {
        return reservationSlotRepository.save(
            ReservationSlot.createWithoutId(reservationDate, reservationTime, theme));
    }

    private void validateThemeAndDateExists(Long themeId, Long dateId) {
        themeRepository.findById(themeId)
            .orElseThrow(() -> new NotFoundException(ThemeErrors.THEME_NOT_EXIST));
        reservationDateRepository.findById(dateId)
            .orElseThrow(() -> new NotFoundException(ReservationDateErrors.RESERVATION_DATE_NOT_EXIST));
    }
}
