package roomescape.reservation;

import java.time.LocalDate;
import roomescape.member.Member;
import roomescape.reservationtime.ReservationTime;
import roomescape.auth.exception.WrongStoreAccessException;
import roomescape.reservation.exception.PastReservationCancelNotAllowedException;
import roomescape.reservation.exception.PastReservationNotAllowedException;
import roomescape.reservation.exception.ReservationOwnerMismatchException;

public class Reservation {

    private final Long id;
    private final Long memberId;
    private final LocalDate date;
    private final ReservationTime time;
    private final Long themeId;
    private final Long storeId;
    private final ReservationStatus status;

    public Reservation(Long id, Long memberId, LocalDate date, ReservationTime time, Long themeId, Long storeId) {
        this(id, memberId, date, time, themeId, storeId, ReservationStatus.CONFIRMED);
    }

    public Reservation(Long id, Long memberId, LocalDate date, ReservationTime time, Long themeId, Long storeId,
                       ReservationStatus status) {
        validateMemberId(memberId);
        validateDate(date);
        validateTime(time);
        validateThemeId(themeId);
        validateStoreId(storeId);
        validateStatus(status);

        this.id = id;
        this.memberId = memberId;
        this.date = date;
        this.time = time;
        this.themeId = themeId;
        this.storeId = storeId;
        this.status = status;
    }

    public static Reservation create(Long memberId, LocalDate date, ReservationTime time, Long themeId, Long storeId) {
        Reservation candidate = new Reservation(null, memberId, date, time, themeId, storeId);
        if (candidate.isPast()) {
            throw new PastReservationNotAllowedException();
        }
        return candidate;
    }

    public static Reservation createPending(Long memberId, LocalDate date, ReservationTime time, Long themeId,
                                            Long storeId) {
        Reservation candidate = new Reservation(
                null, memberId, date, time, themeId, storeId, ReservationStatus.PENDING);
        if (candidate.isPast()) {
            throw new PastReservationNotAllowedException();
        }
        return candidate;
    }

    public Long getId() {
        return id;
    }

    public Long getMemberId() {
        return memberId;
    }

    public ReservationTime getTime() {
        return time;
    }

    public LocalDate getDate() {
        return date;
    }

    public Long getThemeId() {
        return themeId;
    }

    public Long getStoreId() {
        return storeId;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public boolean isConfirmed() {
        return status == ReservationStatus.CONFIRMED;
    }

    public Reservation confirm() {
        return new Reservation(id, memberId, date, time, themeId, storeId, ReservationStatus.CONFIRMED);
    }

    public void validateStoreOwnership(Member member) {
        if (!this.storeId.equals(member.getStoreId())) {
            throw new WrongStoreAccessException();
        }
    }

    public boolean isReservedBy(Long memberId) {
        return this.memberId.equals(memberId);
    }

    public boolean isPast() {
        return this.time.isPastOn(this.date);
    }

    public Reservation changeTo(Long requestingMemberId, LocalDate newDate, ReservationTime newTime) {
        if (!isReservedBy(requestingMemberId)) {
            throw new ReservationOwnerMismatchException();
        }
        Reservation candidate = new Reservation(id, memberId, newDate, newTime, themeId, storeId, status);
        if (candidate.isPast()) {
            throw new PastReservationNotAllowedException();
        }
        return candidate;
    }

    public Reservation changeToByManager(Member manager, LocalDate newDate, ReservationTime newTime) {
        validateStoreOwnership(manager);
        Reservation candidate = new Reservation(id, memberId, newDate, newTime, themeId, storeId, status);
        if (candidate.isPast()) {
            throw new PastReservationNotAllowedException();
        }
        return candidate;
    }

    public void cancelBy(Long requestingMemberId) {
        if (!isReservedBy(requestingMemberId)) {
            throw new ReservationOwnerMismatchException();
        }
        if (isPast()) {
            throw new PastReservationCancelNotAllowedException();
        }
    }

    public Reservation promoteTo(Long newOwnerId) {
        return new Reservation(id, newOwnerId, date, time, themeId, storeId, status);
    }

    private void validateMemberId(Long memberId) {
        if (memberId == null) {
            throw new IllegalArgumentException("회원 ID는 비어 있을 수 없습니다.");
        }
    }

    private void validateDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("날짜는 비어 있을 수 없습니다.");
        }
    }

    private void validateTime(ReservationTime time) {
        if (time == null) {
            throw new IllegalArgumentException("예약시간은 비어 있을 수 없습니다.");
        }
    }

    private void validateThemeId(Long themeId) {
        if (themeId == null) {
            throw new IllegalArgumentException("테마 ID는 비어 있을 수 없습니다.");
        }
    }

    private void validateStoreId(Long storeId) {
        if (storeId == null) {
            throw new IllegalArgumentException("매장 ID는 비어 있을 수 없습니다.");
        }
    }

    private void validateStatus(ReservationStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("예약 상태는 비어 있을 수 없습니다.");
        }
    }
}
