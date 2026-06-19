package roomescape.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import roomescape.exception.DuplicateEntityException;
import roomescape.exception.EntityNotFoundException;
import roomescape.exception.RoomEscapeException;

@Getter
@EqualsAndHashCode(of = "id")
@ToString
public class ReservationSlot {

    private static final int CANCELLABLE_DAYS = 1;

    private final Long id;
    private final Theme theme;
    private final LocalDate date;
    private final ReservationTime time;
    private final Reservations reservations;

    public ReservationSlot(Long id, LocalDate date, Theme theme, ReservationTime time, List<Reservation> reservations) {
        validateReservation(date, theme, time);
        this.id = id;
        this.date = date;
        this.theme = theme;
        this.time = time;
        this.reservations = new Reservations(reservations);
    }

    public static ReservationSlot createSlot(LocalDate date, Theme theme, ReservationTime time) {
        ReservationSlot slot = new ReservationSlot(null, date, theme, time, new ArrayList<>());
        validatePastDateTime(date, time);
        return slot;
    }

    private static void validatePastDateTime(LocalDate date, ReservationTime time) {
        if (time.isPast(date)) {
            throw new RoomEscapeException("이전 날짜로 예약 할 수 없습니다.");
        }
    }

    private static void validateReservation(LocalDate date, Theme theme, ReservationTime time) {
        validateTheme(theme);
        validateReservationDateTime(date, time);
    }

    private static void validateTheme(Theme theme) {
        if (theme == null) {
            throw new RoomEscapeException("테마 정보는 비어있을 수 없습니다.");
        }
    }

    private static void validateReservationDateTime(LocalDate date, ReservationTime time) {
        if (date == null || time == null) {
            throw new RoomEscapeException("예약 날짜 및 시간 정보는 비어있을 수 없습니다.");
        }
    }

    public Reservation reserve(String name) {
        validateNotPast();
        validateDuplicateReservation(name);
        if (reservations.hasReservedReservation()) {
            throw new DuplicateEntityException("이미 예약 된 날짜입니다. (%s %s)", date, time.getStartAt());
        }
        return reservations.addReserved(name);
    }

    public Reservation joinWaitingList(String name) {
        validateNotPast();
        validateDuplicateReservation(name);
        if (reservations.hasReservedReservation()) {
            return reservations.addWaiting(name);
        }
        return reservations.addReserved(name);
    }

    public Reservation pending(String name, ReservationStatus status) {
        validateNotPast();
        validateDuplicateReservation(name);
        return reservations.addPending(name, status);
    }

    public void confirmPendingReservation(long reservationId, ReservationStatus status) {
        Reservation reservation = reservations.findActiveById(reservationId)
                .filter(Reservation::isActivePending)
                .orElseThrow(() -> new EntityNotFoundException("결제 대기 예약 정보를 찾을 수 없습니다."));

        if (status == ReservationStatus.RESERVED) {
            confirmReserved(reservation);
            return;
        }

        if (reservations.hasReservedReservation()) {
            reservation.confirmWaiting();
            return;
        }
        reservation.confirmReserved();
    }

    public void cancelReservation(long reservationId) {
        reservations.findActiveById(reservationId)
                .ifPresent(reservation -> {
                    validateCancelable(reservation);
                    boolean wasReserved = reservation.isActiveReserved();
                    reservation.cancel();
                    promoteFirstWaiting(wasReserved);
                });
    }

    public List<Reservation> getReservations() {
        return reservations.getReservations();
    }

    public Reservation findReservedReservation(long reservationId) {
        return reservations.findActiveById(reservationId)
                .filter(Reservation::isActiveReserved)
                .orElseThrow(() -> new EntityNotFoundException("예약 정보를 찾을 수 없습니다."));
    }

    public Reservation findActiveEntryByName(String name) {
        return reservations.findActiveEntryByName(name)
                .orElseThrow(() -> new EntityNotFoundException("저장된 예약 찾을 수 없습니다."));
    }

    public Reservation findLatestPendingEntryByName(String name) {
        return reservations.findLatestPendingEntryByName(name)
                .orElseThrow(() -> new EntityNotFoundException("저장된 결제 대기 예약을 찾을 수 없습니다."));
    }

    public boolean isSameSlot(LocalDate date, ReservationTime time) {
        return this.date.isEqual(date) && this.time.equals(time);
    }

    private void promoteFirstWaiting(boolean wasReserved) {
        if (wasReserved) {
            reservations.promoteFirstWaiting();
        }
    }

    private void validateNotPast() {
        if (this.time.isPast(this.date)) {
            throw new RoomEscapeException("이미 지난 예약입니다.");
        }
    }

    private void validateDuplicateReservation(String name) {
        if (reservations.hasReservationByName(name)) {
            throw new DuplicateEntityException("이미 예약 또는 대기가 존재합니다. (%s)", name);
        }
    }

    private void confirmReserved(Reservation reservation) {
        if (reservations.hasReservedReservation()) {
            throw new DuplicateEntityException("이미 예약 된 날짜입니다. (%s %s)", date, time.getStartAt());
        }
        reservation.confirmReserved();
    }

    private void validateCancelable(Reservation reservation) {
        if (reservation.isActiveReserved() && canNotCancel()) {
            throw new RoomEscapeException("예약 하루 전에는 취소할 수 없습니다.");
        }
    }

    private boolean canNotCancel() {
        return !date.isAfter(LocalDate.now().plusDays(CANCELLABLE_DAYS));
    }
}
