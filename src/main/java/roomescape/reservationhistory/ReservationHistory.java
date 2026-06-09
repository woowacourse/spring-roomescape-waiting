package roomescape.reservationhistory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.reservation.Reservation;

public class ReservationHistory {

    private final Long id;
    private final Long reservationId;
    private final Long memberId;
    private final LocalDate date;
    private final Long timeId;
    private final Long themeId;
    private final Long storeId;
    private final ReservationHistoryAction action;
    private final Long actorId;
    private final LocalDateTime createdAt;

    public ReservationHistory(Long id, Long reservationId, Long memberId, LocalDate date,
                              Long timeId, Long themeId, Long storeId,
                              ReservationHistoryAction action, Long actorId, LocalDateTime createdAt) {
        validateReservationId(reservationId);
        validateMemberId(memberId);
        validateDate(date);
        validateTimeId(timeId);
        validateThemeId(themeId);
        validateStoreId(storeId);
        validateAction(action);
        validateActorId(actorId);

        this.id = id;
        this.reservationId = reservationId;
        this.memberId = memberId;
        this.date = date;
        this.timeId = timeId;
        this.themeId = themeId;
        this.storeId = storeId;
        this.action = action;
        this.actorId = actorId;
        this.createdAt = createdAt;
    }

    public static ReservationHistory of(Reservation reservation, ReservationHistoryAction action, Long actorId) {
        return new ReservationHistory(
                null,
                reservation.getId(),
                reservation.getMemberId(),
                reservation.getDate(),
                reservation.getTime().getId(),
                reservation.getThemeId(),
                reservation.getStoreId(),
                action,
                actorId,
                null
        );
    }

    public Long getId() {
        return id;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public Long getMemberId() {
        return memberId;
    }

    public LocalDate getDate() {
        return date;
    }

    public Long getTimeId() {
        return timeId;
    }

    public Long getThemeId() {
        return themeId;
    }

    public Long getStoreId() {
        return storeId;
    }

    public ReservationHistoryAction getAction() {
        return action;
    }

    public Long getActorId() {
        return actorId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    private void validateReservationId(Long reservationId) {
        if (reservationId == null) {
            throw new IllegalArgumentException("예약 ID는 비어 있을 수 없습니다.");
        }
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

    private void validateTimeId(Long timeId) {
        if (timeId == null) {
            throw new IllegalArgumentException("예약시간 ID는 비어 있을 수 없습니다.");
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

    private void validateAction(ReservationHistoryAction action) {
        if (action == null) {
            throw new IllegalArgumentException("이력 액션은 비어 있을 수 없습니다.");
        }
    }

    private void validateActorId(Long actorId) {
        if (actorId == null) {
            throw new IllegalArgumentException("수행자 ID는 비어 있을 수 없습니다.");
        }
    }
}
