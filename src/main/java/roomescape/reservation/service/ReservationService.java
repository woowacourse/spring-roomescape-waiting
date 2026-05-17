package roomescape.reservation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.domain.Reservation;
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

import static roomescape.reservation.exception.ReservationErrorCode.*;
import static roomescape.reservationtime.exeption.ReservationTimeErrorCode.*;
import static roomescape.theme.exception.ThemeErrorCode.*;

@Service
@RequiredArgsConstructor
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    private final Clock clock;

    @Transactional
    public Reservation create(String guestName, LocalDate date, Long timeId, Long themeId) {
        ReservationTime time = getReservationTime(timeId);
        Theme theme = getTheme(themeId);

        Reservation reservation = new Reservation(guestName, date, time, theme);

        validateNotDuplicated(reservation);
        validateNotPast(reservation);

        return reservationRepository.save(reservation);
    }

    @Transactional(readOnly = true)
    public List<Reservation> findAllReservations(int page, int size) {
        return reservationRepository.findAll(page, size);
    }

    @Transactional(readOnly = true)
    public List<Reservation> findByGuestName(String guestName) {
        return reservationRepository.findByGuestName(guestName);
    }

    @Transactional
    public Reservation editDateTime(Long reservationId, LocalDate date, Long timeId, String guestName) {
        Reservation reservation = getReservation(reservationId);
        validateIsMyReservation(guestName, reservation);
        validateAlreadyStarted(reservation);

        ReservationTime editedTime = getReservationTime(timeId);
        Reservation editedReservation = createEditedReservation(reservation, date, editedTime);
        validateNotDuplicatedExceptMine(editedReservation);
        validateNotPast(editedReservation);

        updateReservation(editedReservation);

        return editedReservation;
    }

    @Transactional
    public void cancel(Long id) {
        cancelReservation(id);
    }

    @Transactional
    public void deleteMine(Long id, String guestName) {
        Reservation reservation = getReservation(id);

        validateIsMyReservation(guestName, reservation);
        validateAlreadyStarted(reservation);

        cancelReservation(id);
    }

    private void cancelReservation(Long id) {
        if(!reservationRepository.cancelById(id)) { // 위에서 NOT_FOUND를 검증하긴 하지만, 삭제 과정 중에 다른 사람이 변경할 수도 있기에 이중으로 검증
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

    private Reservation createEditedReservation(
            Reservation reservation,
            LocalDate date,
            ReservationTime reservationTime
    ) {
        return new Reservation(
                reservation.getId(),
                reservation.getGuestName(),
                date,
                reservationTime,
                reservation.getTheme()
        );
    }

    private void updateReservation(Reservation reservation) {
        if (!reservationRepository.updateDateAndTime(
                reservation.getId(),
                reservation.getDate(),
                reservation.getTime().getId()
        )) {
            throw new DomainException(RESERVATION_NOT_FOUND);
        }
    }

    private void validateAlreadyStarted(Reservation reservation) {
        if (reservation.isPassed(LocalDateTime.now(clock))) {
            throw new DomainException(CANNOT_EDIT_ALREADY_STARTED_RESERVATION);
        }
    }

    private static void validateIsMyReservation(String guestName, Reservation reservation) {
        if(!reservation.isSameGuest(guestName)) {
            throw new DomainException(CANNOT_EDIT_OTHER_GUEST_RESERVATION);
        }
    }

    private void validateNotPast(Reservation reservation) {
        if (reservation.isPassed(LocalDateTime.now(clock))) {
            throw new DomainException(PAST_RESERVATION_NOT_ALLOWED);
        }
    }

    private void validateNotDuplicated(Reservation reservation) {
        if (reservationRepository.existsByDateAndTimeIdAndThemeId(
                reservation.getDate(),
                reservation.getTime().getId(),
                reservation.getTheme().getId()
        )) {
            throw new DomainException(RESERVATION_ALREADY_EXISTS);
        }
    }

    private void validateNotDuplicatedExceptMine(Reservation reservation) {
        if (reservationRepository.existsByDateAndTimeIdAndThemeIdAndIdNot(
                reservation.getDate(),
                reservation.getTime().getId(),
                reservation.getTheme().getId(),
                reservation.getId()
        )) {
            throw new DomainException(RESERVATION_ALREADY_EXISTS);
        }
    }
}
