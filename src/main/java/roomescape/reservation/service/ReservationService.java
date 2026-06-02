package roomescape.reservation.service;

import static roomescape.reservation.exception.ReservationErrorCode.RESERVATION_NOT_FOUND;
import static roomescape.reservationtime.exeption.ReservationTimeErrorCode.RESERVATION_TIME_NOT_FOUND;
import static roomescape.theme.exception.ThemeErrorCode.THEME_NOT_FOUND;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.DomainException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Status;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.dto.ReservationWaitingResult;
import roomescape.reservation.service.policy.ReservationPolicy;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    private final ReservationPolicy reservationPolicy;

    @Transactional
    public ReservationWaitingResult create(String guestName, LocalDate date, Long timeId, Long themeId) {
        ReservationTime time = getReservationTime(timeId);
        Theme theme = getTheme(themeId);

        Status status = determineState(date, timeId, themeId);

        Reservation reservation = Reservation.create(guestName, date, time, theme, status);

        reservationPolicy.validateCreate(reservation);

        Reservation saved = reservationRepository.save(reservation);

        return reservationRepository.findWaitingById(saved.getId())
                .orElseThrow(() -> new DomainException(RESERVATION_NOT_FOUND));
    }

    public List<Reservation> findAllReservations(int page, int size) {
        return reservationRepository.findAll(page, size);
    }

    public List<ReservationWaitingResult> findByGuestName(String guestName) {
        return reservationRepository.findAllByGuestName(guestName);
    }

    public List<ReservationWaitingResult> findByGuestNameExceptCanceled(String guestName) {
        return reservationRepository.findAllByGuestNameExceptCanceled(guestName);
    }

    @Transactional
    public ReservationWaitingResult editDateTime(Long reservationId, LocalDate date, Long timeId, String guestName) {
        Reservation reservation = getReservation(reservationId);
        ReservationTime changedTime = getReservationTime(timeId);

        Status status = determineState(date, timeId, reservation.themeId());
        Reservation changedReservation = reservation.changeDateAndTime(date, changedTime, status);

        reservationPolicy.validateEdit(reservation, changedReservation, guestName);

        updateReservation(changedReservation);
        promoteWaitingIfNeeded(reservation, changedReservation);

        return reservationRepository.findWaitingById(reservationId)
                .orElseThrow(() -> new DomainException(RESERVATION_NOT_FOUND));
    }

    @Transactional
    public void cancel(Long id) {
        cancelReservation(id);
    }

    @Transactional
    public void deleteMine(Long id, String guestName) {
        Reservation reservation = getReservation(id);
        reservationPolicy.validateDelete(reservation, guestName);
        cancelReservation(id);
    }

    private void cancelReservation(Long id) {
        Reservation reservation = getReservation(id);

        if (!reservationRepository.cancelById(id)) { // 위에서 NOT_FOUND를 검증하긴 하지만, 삭제 과정 중에 다른 사람이 변경할 수도 있기에 이중으로 검증
            throw new DomainException(RESERVATION_NOT_FOUND);
        }

        if (reservation.isConfirmed()) {
            promoteFirstWaiting(reservation.getDate(), reservation.timeId(), reservation.themeId());
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

    private void updateReservation(Reservation reservation) {
        if (!reservationRepository.updateDateAndTime(
                reservation.getId(),
                reservation.getDate(),
                reservation.getTime().getId(),
                reservation.getStatus()
        )) {
            throw new DomainException(RESERVATION_NOT_FOUND);
        }
    }

    private void promoteWaitingIfNeeded(Reservation before, Reservation after) {
        if (!before.isConfirmed()) {
            return;
        }

        if (before.hasSameSlotAs(after)) {
            return;
        }

        promoteFirstWaiting(before.getDate(), before.timeId(), before.themeId());
    }

    private void promoteFirstWaiting(LocalDate date, Long timeId, Long themeId) {
        reservationRepository.findFirstWaitingIdBySlot(date, timeId, themeId)
                .ifPresent(waitingId -> updateState(waitingId, Status.CONFIRMED));
    }

    private void updateState(Long waitingId, Status status) {
        if (!reservationRepository.updateStatus(waitingId, status)) {
            throw new DomainException(RESERVATION_NOT_FOUND);
        }
    }

    private Status determineState(LocalDate date, Long timeId, Long themeId) {
        if (!reservationRepository.existsReservationBySlot(date, timeId, themeId)) {
            return Status.CONFIRMED;
        }
        return Status.WAITING;
    }

}
