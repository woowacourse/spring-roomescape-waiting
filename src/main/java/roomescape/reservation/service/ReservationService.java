package roomescape.reservation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Status;
import roomescape.reservation.service.dto.ReservationWaitingResult;
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
@Transactional(readOnly = true)
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    private final ReservationValidator reservationValidator;
    private final Clock clock;

    @Transactional
    public ReservationWaitingResult create(String guestName, LocalDate date, Long timeId, Long themeId) {
        ReservationTime time = getReservationTime(timeId);
        Theme theme = getTheme(themeId);

        Status status = determineState(date, timeId, themeId);

        Reservation reservation = Reservation.create(guestName, date, time, theme, status, LocalDateTime.now(clock));

        reservationValidator.validateCreate(reservation);

        Reservation saved = reservationRepository.save(reservation);

        return ReservationWaitingResult.from(reservationRepository.findWaitingById(saved.getId())
                .orElseThrow(() -> new DomainException(RESERVATION_NOT_FOUND)));
    }

    public List<Reservation> findAllReservations(int page, int size) {
        return reservationRepository.findAll(page, size);
    }

    public List<ReservationWaitingResult> findByGuestName(String guestName) {
        return reservationRepository.findWaitingAllByGuestName(guestName).stream()
                .map(ReservationWaitingResult::from)
                .toList();
    }

    @Transactional
    public void editDateTime(Long reservationId, LocalDate changedDate, Long timeId, String requestGuestName) {
        Reservation reservation = getReservation(reservationId);
        ReservationTime changedTime = getReservationTime(timeId);

        Reservation beforeReservation = Reservation.clone(reservation);
        Status afterStatus = determineState(changedDate, timeId, reservation.getTheme().getId());
        Reservation changedReservation = reservation.changeDateTimeAndStatus(changedDate, changedTime, afterStatus);

        reservationValidator.validateEdit(reservation, changedReservation, requestGuestName);

        updateDateAndTimeAndStatus(changedReservation);
        updateTopWaitingConfirmed(beforeReservation);
    }

    private void updateTopWaitingConfirmed(Reservation reservation) {
        if(reservation.isConfirmed()) {
            Optional<Reservation> topWaiting = reservationRepository.findBySlotAndStatusWaitingAndWaitingNumberIsOne(
                    reservation.getDate(),
                    reservation.getTimeId(),
                    reservation.getThemeId());

            if(topWaiting.isPresent()) {
                Reservation top = topWaiting.get();
                updateStatus(top.changeStatus(CONFIRMED));
            }
        }
    }

    @Transactional
    public void cancel(Long id) {
        Reservation reservation = getReservation(id);
        cancelReservation(id);
        updateTopWaitingConfirmed(reservation);
    }

    @Transactional
    public void cancelMine(Long id, String guestName) {
        Reservation reservation = getReservation(id);
        reservationValidator.validateDelete(reservation, guestName);
        cancelReservation(id);
    }

    private void cancelReservation(Long id) {
        if(!reservationRepository.cancelById(id)) {
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
                reservation.getStatus()
        )) {
            throw new DomainException(RESERVATION_NOT_FOUND);
        }
    }

    private void updateStatus(Reservation reservation) {
        if (!reservationRepository.updateStatus(reservation.getId(), reservation.getStatus())) {
            throw new DomainException(RESERVATION_NOT_FOUND);
        }
    }

    private Status determineState(LocalDate date, Long timeId, Long themeId){
        if (!reservationRepository.existsBySlot(date, timeId, themeId)){
            return CONFIRMED;
        }
        return Status.WAITING;
    }

}
