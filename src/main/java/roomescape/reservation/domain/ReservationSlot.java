package roomescape.reservation.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.Builder;
import roomescape.global.exception.ConflictException;
import roomescape.global.exception.RoomEscapeException;

public record ReservationSlot(LocalDate date, Long themeId, Long timeId, LocalTime startAt) {

    @Builder
    public ReservationSlot {
        requireDate(date);
        requireTheme(themeId);
        requireTime(timeId);
        requireStartAt(startAt);
    }

    private static void requireDate(LocalDate date) {
        if (date == null) {
            throw new RoomEscapeException("날짜는 비어있을 수 없습니다.");
        }
    }

    private static void requireTheme(Long themeId) {
        if (themeId == null || themeId <= 0) {
            throw new RoomEscapeException("테마ID는 올바른 값이어야 합니다.");
        }
    }

    private static void requireTime(Long timeId) {
        if (timeId == null || timeId <= 0) {
            throw new RoomEscapeException("시간ID는 올바른 값이어야 합니다.");
        }
    }

    private static void requireStartAt(LocalTime startAt) {
        if (startAt == null) {
            throw new RoomEscapeException("시간은 비어있을 수 없습니다.");
        }
    }

    public ReservationSlot updateDateAndTime(LocalDate date, Long timeId, LocalTime startAt) {
        if (this.date.equals(date) && this.timeId.equals(timeId)) {
            throw new ConflictException("동일한 날짜와 시간으로 변경할 수 없습니다.");
        }

        return ReservationSlot.builder()
                .date(date)
                .themeId(this.themeId)
                .timeId(timeId)
                .startAt(startAt)
                .build();
    }

    public void validateReservable(LocalDateTime now) {
        if (LocalDateTime.of(date, startAt).isBefore(now)) {
            throw new RoomEscapeException("현재 시간보다 이전 시간으로 예약을 할 수 없습니다.");
        }
    }

    public void validateDeletable(LocalDateTime now) {
        if (LocalDateTime.of(date, startAt).isBefore(now)) {
            throw new RoomEscapeException("이미 지나간 예약은 삭제할 수 없습니다.");
        }
    }

    public void validatePostponable(LocalDateTime now) {
        if (LocalDateTime.of(date, startAt).isBefore(now)) {
            throw new RoomEscapeException("이미 지나간 예약은 미룰 수 없습니다.");
        }
    }

    public void validateUpdatable(LocalDateTime now) {
        if (LocalDateTime.of(date, startAt).isBefore(now)) {
            throw new RoomEscapeException("이미 지나간 예약은 변경할 수 없습니다.");
        }
    }
}
