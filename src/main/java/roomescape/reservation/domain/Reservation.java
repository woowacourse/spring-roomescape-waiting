package roomescape.reservation.domain;

import lombok.Getter;
import roomescape.global.exception.InvalidRequestException;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
public class Reservation {
    private final Long id;
    private final ReservationStatus status;
    private final Long waitingRank;
    private final String name;
    private final ReservationSlot slot;

    public static Reservation create(String name,
                                     ReservationSlot slot,
                                     LocalDateTime now) {
        validate(now, name, slot);
        return new Reservation(null, null, null, name, slot);
    }

    public Reservation(
            Long id,
            ReservationStatus status,
            Long waitingRank,
            String name,
            ReservationSlot slot) {

        this.id = id;
        this.status = status;
        this.waitingRank = waitingRank;
        this.name = name;
        this.slot = slot;
    }

    public Reservation withId(Long id) {
        validateId(id);

        if (this.id != null) {
            throw new InvalidRequestException("이미 식별자가 존재하는 예약입니다.");
        }

        return new Reservation(id, status, waitingRank, name, slot);
    }

    public Reservation cancel() {
        return new Reservation(id, ReservationStatus.CANCELED, null, name, slot);
    }

    private static void validate(
            LocalDateTime now,
            String name,
            ReservationSlot slot) {
        validateName(name);
        validateSlot(slot);
        validatePastSlot(now, slot);
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidRequestException("예약자 이름은 비어 있을 수 없습니다.");
        }
    }

    private static void validateSlot(ReservationSlot slot) {
        if (slot == null) {
            throw new InvalidRequestException("예약 슬롯은 비어 있을 수 없습니다.");
        }
    }

    private static void validatePastSlot(LocalDateTime now, ReservationSlot slot) {
        if (slot.isPast(now)) {
            throw new InvalidRequestException("현재 시각 이후의 날짜와 시간을 선택해주세요.");
        }
    }

    private void validateId(Long id) {
        if (id == null) {
            throw new InvalidRequestException("예약 id는 비어 있을 수 없습니다.");
        }
    }

    public boolean isPast(LocalDateTime now) {
        return slot.isPast(now);
    }

    public boolean isReservedBy(String name) {
        return Objects.equals(this.name, name);
    }

    public boolean isSameSlot(ReservationSlot slot) {
        return this.slot.isSameSlot(slot);
    }

    public boolean isCanceled() {
        return this.status == ReservationStatus.CANCELED;
    }

    public Theme getTheme() {
        return slot.theme();
    }

    public LocalDate getDate() {
        return slot.date();
    }

    public ReservationTime getTime() {
        return slot.time();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Reservation that)) return false;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
