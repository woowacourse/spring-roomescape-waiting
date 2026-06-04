package roomescape.reservation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.dto.PageResult;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Status;
import roomescape.reservation.repository.exception.RetryableReservationCreateException;
import roomescape.reservation.service.dto.ReservationWaitingResult;
import roomescape.common.retry.RetryOnException;
import roomescape.reservation.service.validator.ReservationValidator;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.common.exception.DomainException;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.repository.ThemeRepository;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static roomescape.reservation.domain.Status.CONFIRMED;
import static roomescape.reservation.exception.ReservationErrorCode.*;
import static roomescape.reservationtime.exeption.ReservationTimeErrorCode.*;
import static roomescape.theme.exception.ThemeErrorCode.*;

@Service
@RequiredArgsConstructor
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    private final ReservationValidator reservationValidator;
    private final Clock clock;
    private final ReservationCreator reservationCreator;

    public ReservationWaitingResult create(String guestName, LocalDate date, Long timeId, Long themeId) {
        ReservationTime time = getReservationTime(timeId);
        Theme theme = getTheme(themeId);
        Reservation saved = reservationCreator.createReservation(guestName, date, time, theme);
        return ReservationWaitingResult.from(reservationRepository.findWaitingById(saved.getId())
                .orElseThrow(() -> new DomainException(RESERVATION_NOT_FOUND)));
    }

    @Transactional(readOnly = true)
    public PageResult<Reservation> findAllReservations(int page, int size) {
        return reservationRepository.findAllByStatusCanceledNot(page, size);
    }

    @Transactional(readOnly = true)
    public List<ReservationWaitingResult> findByGuestName(String guestName) {
        return reservationRepository.findWaitingAllByGuestName(guestName).stream()
                .map(ReservationWaitingResult::from)
                .toList();
    }

    @Transactional
    public void editDateTime(Long reservationId, LocalDate changedDate, Long changedTimeId, String requestGuestName) {
        Reservation beforeReservation = getReservation(reservationId);
        reservationValidator.validateBeforeEdit(beforeReservation, changedDate, changedTimeId, requestGuestName);

        ReservationTime changedTime = getReservationTime(changedTimeId);

        Status afterStatus = determineState(changedDate, changedTimeId, beforeReservation.getTheme().getId());
        Reservation changedReservation = beforeReservation.changeDateTimeAndStatus(
                changedDate, changedTime, afterStatus, LocalDateTime.now(clock));

        reservationValidator.validateEdit(changedReservation);

        updateDateAndTimeAndStatus(changedReservation);
        updateTopWaitingConfirmed(beforeReservation);
    }

    private void updateTopWaitingConfirmed(Reservation reservation) {
        if (reservation.isConfirmed()) {
            Optional<Reservation> topWaiting = reservationRepository.findBySlotAndStatusWaitingAndWaitingNumberIsOne(
                    reservation.getDate(),
                    reservation.getTimeId(),
                    reservation.getThemeId());

            if (topWaiting.isPresent()) {
                Reservation top = topWaiting.get();
                updateStatus(top.changeStatus(CONFIRMED));
            }
        }
    }

    @Transactional
    public void cancel(Long id) {
        Reservation reservation = getReservation(id);
        reservationValidator.validateCancel(reservation);
        cancelReservation(id);
        updateTopWaitingConfirmed(reservation);
    }

    @Transactional
    public void cancelMine(Long id, String guestName) {
        Reservation reservation = getReservation(id);
        reservationValidator.validateCancelMine(reservation, guestName);
        cancelReservation(id);
        updateTopWaitingConfirmed(reservation);
    }

    private void cancelReservation(Long id) {
        if (!reservationRepository.cancelById(id)) {
            throw new DomainException(RESERVATION_NOT_FOUND);
        }
    }

    private Theme getTheme(Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new DomainException(THEME_NOT_FOUND));
    }

    private Reservation getReservation(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new DomainException(RESERVATION_NOT_FOUND));
    }

    private ReservationTime getReservationTime(Long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new DomainException(RESERVATION_TIME_NOT_FOUND));
    }

    private void updateDateAndTimeAndStatus(Reservation reservation) {
        if (!reservationRepository.updateDateAndTimeAndStatus(
                reservation.getId(),
                reservation.getDate(),
                reservation.getTime().getId(),
                reservation.getStatus(),
                reservation.getLastModifiedAt()
        )) {
            throw new DomainException(RESERVATION_NOT_FOUND);
        }
    }

    private void updateStatus(Reservation reservation) {
        if (!reservationRepository.updateStatus(reservation.getId(), reservation.getStatus())) {
            throw new DomainException(RESERVATION_NOT_FOUND);
        }
    }

    private Status determineState(LocalDate date, Long timeId, Long themeId) {
        if (!reservationRepository.existsBySlotAndStatusConfirmed(date, timeId, themeId)) {
            return CONFIRMED;
        }
        return Status.WAITING;
    }
}
