package roomescape.reservation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.dto.PageResult;
import roomescape.common.exception.DomainException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.domain.Status;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationSlotRepository;
import roomescape.reservation.service.dto.ReservationWaitingResult;
import roomescape.reservation.service.validator.ReservationValidator;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.repository.ThemeRepository;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static roomescape.reservation.domain.Status.CONFIRMED;
import static roomescape.reservation.exception.ReservationErrorCode.*;
import static roomescape.reservationtime.exeption.ReservationTimeErrorCode.*;
import static roomescape.theme.exception.ThemeErrorCode.*;

@Service
@RequiredArgsConstructor
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ReservationSlotRepository reservationSlotRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    private final ReservationValidator reservationValidator;
    private final Clock clock;
    private final ReservationCreator reservationCreator;

    public ReservationWaitingResult create(String guestName, LocalDate date, Long timeId, Long themeId) {
        ReservationTime time = getReservationTime(timeId);
        Theme theme = getTheme(themeId);
        ReservationSlot reservationSlot = reservationSlotRepository.upsert(ReservationSlot.create(date, time, theme));
        lockSlot(reservationSlot.getId());

        Reservation saved = reservationCreator.createReservation(guestName, reservationSlot);
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

        ReservationSlot changedSlot = reservationSlotRepository.upsert(
                ReservationSlot.create(changedDate, changedTime, beforeReservation.getTheme()));
        lockSlots(beforeReservation.getReservationSlot(), changedSlot);

        Status afterStatus = determineState(changedSlot);
        Reservation changedReservation = Reservation.of(
                beforeReservation.getId(),
                beforeReservation.getGuestName(),
                changedSlot,
                afterStatus,
                LocalDateTime.now(clock));

        reservationValidator.validateEdit(changedReservation);

        updateSlotAndStatus(changedReservation, changedSlot);
        updateTopWaitingConfirmed(beforeReservation);
    }

    private void lockSlots(ReservationSlot first, ReservationSlot second) {
        Stream.of(first, second)
                .map(ReservationSlot::getId)
                .distinct()
                .sorted(Comparator.naturalOrder())
                .forEach(this::lockSlot);
    }

    private void lockSlot(Long slotId) {
        reservationSlotRepository.findByIdWithLock(slotId)
                .orElseThrow(() -> new DomainException(RESERVATION_SLOT_NOT_FOUND));
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
        lockSlot(reservation.getReservationSlot().getId());
        reservationValidator.validateCancel(reservation);
        cancelReservation(id);
        updateTopWaitingConfirmed(reservation);
    }

    @Transactional
    public void cancelMine(Long id, String guestName) {
        Reservation reservation = getReservation(id);
        lockSlot(reservation.getReservationSlot().getId());
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

    private void updateSlotAndStatus(Reservation reservation, ReservationSlot slot) {
        if (!reservationRepository.updateSlotAndStatus(
                reservation.getId(),
                slot.getId(),
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

    private Status determineState(ReservationSlot slot) {
        if (!reservationRepository.existsBySlotAndStatusConfirmed(slot)) {
            return CONFIRMED;
        }
        return Status.WAITING;
    }
}
