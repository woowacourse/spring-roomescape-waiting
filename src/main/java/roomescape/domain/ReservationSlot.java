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

    public boolean isSameSlot(LocalDate date, ReservationTime time) {
        return this.date.isEqual(date) && this.time.equals(time);
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

    public List<Reservation> getReservations() {
        return reservations.getReservations();
    }

    private void validateNotPast() {
        if (this.time.isPast(this.date)) {
            throw new RoomEscapeException("이미 지난 예약입니다.");
        }
    }

    private void validateDuplicateReservation(String name) {
        if (reservations.hasActiveReservationByName(name)) {
            throw new DuplicateEntityException("이미 예약 또는 대기가 존재합니다. (%s)", name);
        }
    }

    public Reservation findReservedReservation(long reservationId) {
        return reservations.findById(reservationId)
                .filter(Reservation::isReserved)
                .orElseThrow(() -> new EntityNotFoundException("예약 정보를 찾을 수 없습니다."));
    }

    public Reservation findReservationByNameAndStatus(String name, ReservationStatus status) {
        return reservations.findByNameAndStatus(name, status)
                .orElseThrow(() -> new EntityNotFoundException("저장된 예약 찾을 수 없습니다."));
    }

    public void cancelReservation(long reservationId) {
        Reservation reservation = reservations.findById(reservationId)
                .orElseThrow(() -> new EntityNotFoundException("예약 정보를 찾을 수 없습니다."));

        boolean wasReserved = reservation.isReserved();
        reservation.cancel();

        if (wasReserved) {
            reservations.promoteFirstWaiting();
        }
    }
}
