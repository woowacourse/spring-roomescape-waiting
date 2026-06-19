package roomescape.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.WaitingListRepository;
import roomescape.service.dto.ReservationAvailableEvent;
import roomescape.service.dto.ReservationConfirmationEvent;
import roomescape.service.dto.command.ReservationCreateCommand;
import roomescape.service.dto.command.ReservationDeleteCommand;
import roomescape.service.dto.command.ReservationModifyCommand;
import roomescape.service.dto.result.AvailableDateResult;
import roomescape.service.dto.result.ReservationResult;
import roomescape.service.dto.result.ReservationTimeStatusResult;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final WaitingListRepository waitingListRepository;
    private final ApplicationEventPublisher eventPublisher;

    private static final int RESERVABLE_DAYS_RANGE = 14;

    public ReservationResult create(final ReservationCreateCommand data) {
        final LocalDate date = data.date();
        validateFutureOrPresentDate(date);
        final Long timeId = data.timeId();
        final ReservationTime reservationTime = getReservationTime(timeId);
        validateFutureOrPresentTime(date, reservationTime);
        final Long themeId = data.themeId();
        final Theme theme = getTheme(themeId);

        validateAvailable(date, timeId, themeId);

        final Reservation newReservation = Reservation.prepare(data.name(), date, reservationTime, theme);

        final Reservation savedReservation = reservationRepository.save(newReservation);
        return ReservationResult.from(savedReservation);
    }

    @Transactional
    public ReservationResult modify(final ReservationModifyCommand reservationModifyCommand) {
        final Long reservationId = reservationModifyCommand.reservationId();
        final Reservation originalReservation = getReservation(reservationId);
        final String personName = reservationModifyCommand.name();
        validateReservationOwner(originalReservation, personName);

        final LocalDate originalDate = originalReservation.getDate();
        final ReservationTime originalTime = originalReservation.getTime();
        validateFutureOrPresentDate(originalDate);
        validateFutureOrPresentTime(originalDate, originalTime);

        final Long timeId = Objects.requireNonNullElse(
                reservationModifyCommand.timeId(),
                originalTime.getId()
        );
        final ReservationTime reservationTime = getReservationTime(timeId);
        final Reservation modifiedReservation = originalReservation.modifyDateAndTime(
                reservationModifyCommand.date(),
                reservationTime
        );

        final LocalDate date = modifiedReservation.getDate();
        validateFutureOrPresentDate(date);
        validateFutureOrPresentTime(date, reservationTime);
        validateAvailable(date, timeId, modifiedReservation.getTheme().getId());

        reservationRepository.update(modifiedReservation);
        callEventListenerForWaitingListApproval(originalReservation);
        return ReservationResult.from(modifiedReservation);
    }

    @Transactional
    public void delete(final Long reservationId) {
        final Reservation reservation = getReservation(reservationId);
        deleteReservation(reservationId);
        callEventListenerForWaitingListApproval(reservation);
    }

    @Transactional
    public void deleteWithValidation(final ReservationDeleteCommand deleteCommand) {
        final Reservation reservation = getReservation(deleteCommand.reservationId());
        validateReservationOwner(reservation, deleteCommand.name());
        final LocalDate date = reservation.getDate();
        validateFutureOrPresentDate(date);
        final ReservationTime reservationTime = reservation.getTime();
        validateFutureOrPresentTime(date, reservationTime);

        deleteReservation(reservation.getId());
        callEventListenerForWaitingListApproval(reservation);
    }

    private void deleteReservation(final Long reservationId) {
        final boolean deleted = reservationRepository.deleteById(reservationId);
        if (!deleted) {
            throw new BusinessException(ErrorCode.RESERVATION_NOT_FOUND);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void confirmReservation(final ReservationConfirmationEvent event) {
        reservationRepository.updateStatusById(event.reservationId(), ReservationStatus.CONFIRMED);
    }

    public List<ReservationResult> getReservations() {
        return reservationRepository.findAll()
                .stream()
                .map(ReservationResult::from)
                .toList();
    }

    public List<ReservationTimeStatusResult> getReservationTimeStatuses(final LocalDate date, final Long themeId) {
        return reservationRepository.findReservationTimeStatusesByDateAndThemeId(date, themeId)
                .stream()
                .map(reservationTimesWithStatus -> new ReservationTimeStatusResult(
                        reservationTimesWithStatus.id(),
                        reservationTimesWithStatus.startAt(),
                        reservationTimesWithStatus.reserved()
                ))
                .toList();
    }

    public AvailableDateResult getReservationOptions() {
        final LocalDate today = LocalDate.now();
        final List<LocalDate> dates = today.datesUntil(today.plusDays(RESERVABLE_DAYS_RANGE)).toList();

        return new AvailableDateResult(dates);
    }

    public List<ReservationResult> getReservationsByName(final String name) {
        final List<Reservation> reservations = reservationRepository.findByName(name);
        return reservations.stream()
                .map(ReservationResult::from)
                .toList();
    }

    private void callEventListenerForWaitingListApproval(final Reservation reservation) {
        eventPublisher.publishEvent(new ReservationAvailableEvent(
                        reservation.getDate(),
                        reservation.getTime().getId(),
                        reservation.getTheme().getId()
                )
        );
    }

    private Reservation getReservation(final Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));
    }

    private ReservationTime getReservationTime(final Long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TIME_NOT_FOUND));
    }

    private Theme getTheme(final Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.THEME_NOT_FOUND));
    }

    private void validateFutureOrPresentDate(final LocalDate date) {
        final LocalDate today = LocalDate.now();
        if (date.isBefore(today)) {
            throw new BusinessException(ErrorCode.DATE_ALREADY_PASSED);
        }
    }

    private void validateFutureOrPresentTime(final LocalDate date, final ReservationTime reservationTime) {
        final LocalDate today = LocalDate.now();
        final LocalTime now = LocalTime.now();
        if (date.equals(today) && reservationTime.isBefore(now)) {
            throw new BusinessException(ErrorCode.TIME_ALREADY_PASSED);
        }
    }

    private void validateAvailable(final LocalDate date, final Long timeId, final Long themeId) {
        final boolean isAlreadyReserved = reservationRepository.existsByDateAndTimeIdAndThemeId(date, timeId, themeId);
        if (isAlreadyReserved) {
            throw new BusinessException(ErrorCode.TIME_ALREADY_RESERVED);
        }

        final boolean hasWaitingList = waitingListRepository.existsByDateAndTimeIdAndThemeId(date, timeId, themeId);
        if (hasWaitingList) {
            throw new BusinessException(ErrorCode.QUEUED_WAITING_LIST);
        }
    }

    private void validateReservationOwner(final Reservation reservation, final String name) {
        final boolean isUserNameMatched = reservation.isOwner(name);

        if (!isUserNameMatched) {
            throw new BusinessException(ErrorCode.USER_NAME_NOT_MATCHED);
        }
    }
}
