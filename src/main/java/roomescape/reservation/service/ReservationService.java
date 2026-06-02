package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.ConflictException;
import roomescape.global.exception.InvalidBusinessStateException;
import roomescape.global.exception.NotFoundException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.exception.ReservationErrorCode;
import roomescape.reservation.service.dto.PopularThemesResult;
import roomescape.reservation.service.dto.ReservationCommand;
import roomescape.reservation.service.dto.ReservationResult;
import roomescape.reservation.service.dto.ReservationUpdateCommand;
import roomescape.reservation.service.dto.ReservationWithStatusResult;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.ThemeService;
import roomescape.time.domain.ReservationTime;
import roomescape.time.service.ReservationTimeService;
import roomescape.waiting.domain.ReservationWaiting;
import roomescape.waiting.domain.ReservationWaitingRepository;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final ThemeService themeService;
    private final ReservationTimeService reservationTimeService;
    private final ReservationRepository reservationRepository;
    private final ReservationWaitingRepository reservationWaitingRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationTimeService reservationTimeService,
                              ThemeService themeService,
                              ReservationWaitingRepository reservationWaitingRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeService = reservationTimeService;
        this.themeService = themeService;
        this.reservationWaitingRepository = reservationWaitingRepository;
    }

    @Transactional
    public ReservationResult save(ReservationCommand command) {
        try {
            Reservation saved = reservationRepository.save(buildNewReservation(command));
            return ReservationResult.from(saved);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException(ReservationErrorCode.DUPLICATE_RESERVATION.getMessage());
        }
    }

    @Transactional
    public void update(ReservationUpdateCommand command, Long id, String name) {
        Reservation updated = buildUpdatedReservation(command, id, name);
        try {
            reservationRepository.update(updated);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException(ReservationErrorCode.DUPLICATE_RESERVATION.getMessage());
        }
    }

    @Transactional
    public void deleteById(Long id, String name) {
        Reservation reservation = getById(id);
        if (name != null) {
            reservation.validateOwner(name);
        }
        reservation.validateExpiry();

        List<ReservationWaiting> waitings = reservationWaitingRepository.findAllByDateAndTimeIdAndThemeIdForUpdate(
                reservation.getDate(),
                reservation.getTimeId(),
                reservation.getThemeId()
        );

        reservationRepository.delete(reservation);
        promoteNextWaiting(waitings);
    }

    public List<ReservationResult> findAll() {
        return reservationRepository.findAll().stream()
                .map(ReservationResult::from)
                .toList();
    }

    public List<ReservationWithStatusResult> findAllByName(String name) {
        return reservationRepository.findAllByNameWithStatus(name);
    }

    public PopularThemesResult findPopularThemes(int period, int limit) {
        int oneDayDifference = 1;
        LocalDate to = LocalDate.now().minusDays(oneDayDifference);
        LocalDate from = to.minusDays(period).plusDays(oneDayDifference);
        return new PopularThemesResult(
                reservationRepository.findPopularThemes(from, to, limit)
        );
    }

    public Reservation getById(Long id) {
        return reservationRepository.findById(id).orElseThrow(
                () -> new NotFoundException(ReservationErrorCode.RESERVATION_NOT_FOUND.getMessage())
        );
    }

    private Reservation buildNewReservation(ReservationCommand command) {
        ReservationTime time = reservationTimeService.getById(command.timeId());
        Theme theme = themeService.findById(command.themeId());

        Reservation newReservation = Reservation.of(command.name(), command.date(), time, theme);
        validateNoDoubleBooking(command.date(), time, command.name());

        return newReservation;
    }

    private void validateNoDoubleBooking(LocalDate date, ReservationTime time, String name) {
        validateNoSameTimeBooking(date, time, name);
        validateNoSameTimeWaiting(date, time.getId(), name);
    }

    private void validateNoSameTimeBooking(LocalDate date, ReservationTime time, String name) {
        if (reservationRepository.existsByDateAndTimeIdAndName(date, time.getId(), name)) {
            throw new InvalidBusinessStateException(
                    ReservationErrorCode.ALREADY_RESERVED_OR_WAITING_AT_SAME_TIME.getMessage());
        }
    }

    private void validateNoSameTimeWaiting(LocalDate date, Long timeId, String name) {
        if (reservationWaitingRepository.existsByDateAndTimeIdAndName(date, timeId, name)) {
            throw new InvalidBusinessStateException(
                    ReservationErrorCode.ALREADY_RESERVED_OR_WAITING_AT_SAME_TIME.getMessage());
        }
    }

    private Reservation buildUpdatedReservation(ReservationUpdateCommand command, Long id, String name) {
        Reservation reservation = getById(id);
        ReservationTime newTime = getReservationTime(command.timeId());
        Reservation updated = reservation.update(command.date(), newTime, name);
        validateNoDoubleBookingForUpdate(updated.getDate(), updated.getTime(), name, id);
        return updated;
    }

    private ReservationTime getReservationTime(Long timeId) {
        if (timeId == null) {
            return null;
        }
        return reservationTimeService.getById(timeId);
    }

    private void validateNoDoubleBookingForUpdate(LocalDate date, ReservationTime time, String name,
                                                  Long excludeId) {
        if (reservationRepository.existsByDateAndTimeIdAndNameAndIdNot(date, time.getId(), name, excludeId)) {
            throw new InvalidBusinessStateException(
                    ReservationErrorCode.ALREADY_RESERVED_OR_WAITING_AT_SAME_TIME.getMessage());
        }
        validateNoSameTimeWaiting(date, time.getId(), name);
    }

    private void promoteNextWaiting(List<ReservationWaiting> lockedWaitings) {
        for (ReservationWaiting waiting : lockedWaitings) {
            if (!reservationRepository.existsByDateAndTimeIdAndName(
                    waiting.getDate(),
                    waiting.getTime().getId(),
                    waiting.getName())
            ) {
                createReservationFromWaiting(waiting);
                return;
            }
        }
    }

    private void createReservationFromWaiting(ReservationWaiting waiting) {
        reservationWaitingRepository.deleteById(waiting.getId());

        Reservation newReservation = Reservation.of(
                waiting.getName(),
                waiting.getDate(),
                waiting.getTime(),
                waiting.getTheme()
        );
        reservationRepository.save(newReservation);
    }
}
