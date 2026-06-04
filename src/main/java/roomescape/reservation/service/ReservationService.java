package roomescape.reservation.service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.ConflictException;
import roomescape.global.exception.InvalidBusinessStateException;
import roomescape.global.exception.NotFoundException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.exception.ReservationErrorCode;
import roomescape.reservation.service.dto.ReservationCommand;
import roomescape.reservation.service.dto.ReservationResult;
import roomescape.reservation.service.dto.ReservationUpdateCommand;
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
    public ReservationResult save(ReservationCommand command, LocalDateTime requestTime) {
        try {
            Reservation saved = buildNewReservation(command, requestTime);
            saved.validateExpiry(requestTime);
            saved = reservationRepository.save(saved);
            return ReservationResult.from(saved);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException(ReservationErrorCode.DUPLICATE_RESERVATION);
        }
    }

    @Transactional
    public void update(ReservationUpdateCommand command, Long id, String name, LocalDateTime requestTime) {
        Reservation updated = buildUpdatedReservation(command, id, name, requestTime);
        updated.validateExpiry(requestTime);
        try {
            reservationRepository.save(updated);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException(ReservationErrorCode.DUPLICATE_RESERVATION);
        }
    }

    @Transactional
    public void deleteById(Long id, String name, LocalDateTime requestTime) {
        Reservation reservation = getById(id);
        if (name != null) {
            reservation.validateOwner(name);
        }
        reservation.validateExpiry(requestTime);

        reservationTimeService.getByIdForUpdate(reservation.getTimeId());

        List<ReservationWaiting> waitings = reservationWaitingRepository.queryAllBySlotForUpdate(
                new ReservationSlot(reservation.getDate(), reservation.getTime(), reservation.getTheme())
        );

        reservationRepository.delete(reservation);
        promoteNextWaiting(waitings, requestTime);
    }

    public Reservation getById(Long id) {
        return reservationRepository.findById(id).orElseThrow(
                () -> new NotFoundException(ReservationErrorCode.RESERVATION_NOT_FOUND)
        );
    }

    private Reservation buildNewReservation(ReservationCommand command, LocalDateTime requestTime) {
        ReservationTime time = reservationTimeService.getByIdForUpdate(command.timeId());
        Theme theme = themeService.findById(command.themeId());

        Reservation newReservation = new Reservation(command.name(), command.date(), time, theme, requestTime);
        validateNoDoubleBooking(newReservation);

        return newReservation;
    }

    private void validateNoDoubleBooking(Reservation newReservation) {
        validateNoSameTimeBooking(newReservation);
        validateNoSameTimeWaiting(newReservation);
    }

    private void validateNoSameTimeBooking(Reservation newReservation) {
        if (reservationRepository.hasBookingAtSameTime(newReservation)) {
            throw new InvalidBusinessStateException(
                    ReservationErrorCode.ALREADY_RESERVED_OR_WAITING_AT_SAME_TIME);
        }
    }

    private void validateNoSameTimeWaiting(Reservation newReservation) {
        ReservationWaiting dummy = new ReservationWaiting(
                null,
                newReservation.getName(),
                newReservation.getSlot(),
                newReservation.getUpdatedAt()
        );
        if (reservationWaitingRepository.hasWaitingAtSameTime(dummy)) {
            throw new InvalidBusinessStateException(
                    ReservationErrorCode.ALREADY_RESERVED_OR_WAITING_AT_SAME_TIME);
        }
    }

    private Reservation buildUpdatedReservation(ReservationUpdateCommand command, Long id, String name,
                                                LocalDateTime requestTime) {
        Reservation reservation = getById(id);
        reservation.validateExpiry(requestTime);
        ReservationTime newTime = getReservationTime(command.timeId());
        Reservation updated = reservation.update(command.date(), newTime, name, requestTime);
        updated.validateExpiry(requestTime);
        validateNoDoubleBookingForUpdate(updated);
        return updated;
    }

    private ReservationTime getReservationTime(Long timeId) {
        if (timeId == null) {
            return null;
        }
        return reservationTimeService.getByIdForUpdate(timeId);
    }

    private void validateNoDoubleBookingForUpdate(Reservation updated) {
        if (reservationRepository.isAlreadyBookedByOthers(updated)) {
            throw new InvalidBusinessStateException(
                    ReservationErrorCode.ALREADY_RESERVED_OR_WAITING_AT_SAME_TIME);
        }
        validateNoSameTimeWaiting(updated);
    }

    private void promoteNextWaiting(List<ReservationWaiting> lockedWaitings, LocalDateTime requestTime) {
        for (ReservationWaiting waiting : lockedWaitings) {
            Reservation candidate = new Reservation(null, waiting.getName(), waiting.getSlot(),
                    waiting.getSlot().date().atStartOfDay());
            if (!reservationRepository.hasBookingAtSameTime(candidate)) {
                createReservationFromWaiting(waiting, requestTime);
                return;
            }
        }
    }

    private void createReservationFromWaiting(ReservationWaiting waiting, LocalDateTime requestTime) {
        reservationWaitingRepository.delete(waiting);
        Reservation newReservation = new Reservation(
                waiting.getName(),
                waiting.getDate(),
                waiting.getTime(),
                waiting.getTheme(),
                requestTime
        );
        newReservation.validateExpiry(requestTime);
        reservationRepository.save(newReservation);
    }
}
